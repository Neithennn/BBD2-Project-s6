# Detail pas-a-pas des scenarios transactionnels

Fichier de reference : `sql/test_transactions.sql`

## Concepts utilises

- `START TRANSACTION` / `COMMIT` / `ROLLBACK`
- `SIGNAL SQLSTATE '45000'` pour lever une erreur metier
- `DECLARE EXIT HANDLER FOR SQLEXCEPTION` pour capturer l'erreur et faire le ROLLBACK
- Procedures stockees pour encapsuler les scenarios (necessaire pour que le HANDLER
  fonctionne dans le contexte du client MySQL)

## Etat initial attendu (affiche en debut de script)

Le script commence par afficher l'etat des ateliers et leur nombre de reservations.

| id | title | max | nb_reservations |
|---|---|---|---|
| 1 | Maîtriser la peinture à l huile | 3 | 2 |
| 2 | Paysages impressionnistes       | 5 | 1 |
| 3 | Sculpture en argile             | 4 | 2 |

## Scenario 1 - Inscriptions multiples reussies (COMMIT)

But : demontrer qu'une transaction peut englober plusieurs operations et les valider
en une fois si tout reussit.

```sql
START TRANSACTION;
    CALL sp_inscrire_membre(5, 2);   -- membre 5 dans atelier 2 (OK, places dispo)
    CALL sp_inscrire_membre(5, 3);   -- membre 5 dans atelier 3 (OK, places dispo)
COMMIT;
```

Attendu :
- Les 2 inscriptions sont persistees.
- `SELECT * FROM bookings WHERE member_id = 5;` renvoie 2 lignes.

## Scenario 2 - Doublon detecte (ROLLBACK automatique)

But : demontrer qu'une erreur metier (ici un doublon) declenche un ROLLBACK qui
annule *toutes* les operations de la transaction, pas juste celle qui a echoue.

```sql
CREATE PROCEDURE test_scenario_2()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'Scenario 2 : erreur captee, ROLLBACK effectue' AS Resultat;
    END;

    START TRANSACTION;
        CALL sp_inscrire_membre(3, 2);   -- OK : membre 3 pas encore dans atelier 2
        CALL sp_inscrire_membre(1, 1);   -- KO : membre 1 deja dans atelier 1
    COMMIT;
END$$
```

Deroulement :
1. `sp_inscrire_membre(3, 2)` passe sans erreur, la ligne est ecrite en memoire.
2. `sp_inscrire_membre(1, 1)` detecte le doublon et fait `SIGNAL SQLSTATE '45000'`.
3. L'exception remonte -> le HANDLER s'execute -> `ROLLBACK`.
4. **La ligne du point 1 n'est pas persistee** : la transaction est annulee en entier.

Verification apres execution :
```sql
SELECT COUNT(*) FROM bookings WHERE member_id = 3 AND workshop_id = 2;
-- Doit renvoyer 0
```

## Scenario 3 - Capacite atteinte (trigger + ROLLBACK)

But : demontrer la cooperation trigger SQL + transaction client.

Contexte : l'atelier 1 a `max_participants = 3` et 2 reservations deja faites.

```sql
CREATE PROCEDURE test_scenario_3()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'Scenario 3 : capacite atteinte, ROLLBACK effectue' AS Resultat;
    END;

    START TRANSACTION;
        CALL sp_inscrire_membre(4, 1);   -- 3e inscription (OK, on atteint exactement 3/3)
        CALL sp_inscrire_membre(5, 1);   -- 4e inscription : le trigger bloque
    COMMIT;
END$$
```

Deroulement :
1. `sp_inscrire_membre(4, 1)` : pas de doublon, INSERT dans `bookings`. Le trigger
   `trg_verif_capacite_workshop` compte 2 inscriptions existantes, 2 < 3, l'INSERT passe.
2. `sp_inscrire_membre(5, 1)` : pas de doublon, INSERT. Le trigger compte 3 inscriptions
   existantes, 3 >= 3 -> `SIGNAL SQLSTATE '45000'`.
3. Le HANDLER capture -> `ROLLBACK` -> la 3e inscription (Diana) n'est pas persistee.

Verification :
```sql
SELECT COUNT(*) FROM bookings WHERE member_id = 4 AND workshop_id = 1;
-- Doit renvoyer 0
```

## Pourquoi encapsuler dans une procedure stockee ?

En MySQL, les erreurs levees par `SIGNAL` interrompent l'execution cote client si
elles ne sont pas attrapees. Comme la version `mysql -u root -p ... < script.sql`
s'arrete a la premiere erreur, on wrap chaque scenario dans une procedure pour
pouvoir y placer un `HANDLER`. Sans ca, le script s'arreterait a la 2e ligne du
scenario 2 ou 3 sans jouer la suite.

## Cote Java - transactions JDBC

Meme mecanique dans les DAOs, dans la methode `save()` :

```java
conn.setAutoCommit(false);
try {
    // INSERT artists
    // ... recuperation de l'id genere
    // INSERT artist_disciplines
    conn.commit();
} catch (SQLException e) {
    conn.rollback();
    throw e;
} finally {
    conn.setAutoCommit(true);
}
```

Fichiers : `JdbcArtistDao.java`, `JdbcArtworkDao.java`, `JdbcExhibitionDao.java`.
