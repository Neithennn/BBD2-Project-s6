# Script de demo devant le prof

> Objectif : faire une demo de 10-15 minutes qui couvre tout le bareme de l'Etape 3.
> Chaque bloc = une commande a lancer + ce que tu dois dire/montrer.

## Preparation AVANT l'entretien

1. **Ouvre 2 terminaux cote a cote**
   - Terminal 1 : shell pour lancer les scripts `.sql`
   - Terminal 2 : client MySQL interactif (`mysql -u root -p ArtConnect`) pour les tests manuels

2. **Verifie que MySQL tourne**
   ```bash
   mysql --version
   sudo service mysql status   # ou : brew services list sur Mac
   ```

3. **Va a la racine du projet**
   ```bash
   cd chemin/vers/BBD2-Project-s6-master
   ```

4. **Nettoie tout pour partir d'une base propre** (optionnel si tu l'as deja fait)
   ```bash
   mysql -u root -p -e "DROP DATABASE IF EXISTS ArtConnect;"
   ```

5. **Ouvre en parallele dans VS Code** :
   - `sql/schema.sql`
   - `sql/advanced.sql`
   - `sql/test_transactions.sql`
   - `docs/etape3_verification/01_checklist_bareme.md`

## Etape 1 - Schema (1-2 min)

**Dire :** "Je commence par creer la base et les tables. Le schema respecte la 3FN
avec 15 tables dont 4 de jonction pour les N:N et 1 d'audit."

**Lancer :**
```bash
mysql -u root -p < sql/schema.sql
```

**Montrer :**
```bash
mysql -u root -p ArtConnect -e "SHOW TABLES;"
```

**Attendu :** 15 tables listees.

**Eventuellement :** ouvrir `sql/schema.sql` dans VS Code et montrer les contraintes
`UNIQUE`, `CHECK (rating BETWEEN 1 AND 5)`, `FOREIGN KEY ... ON DELETE CASCADE`.

## Etape 2 - Donnees d'exemple (1 min)

**Dire :** "J'ai genere les donnees via un LLM. Le prompt complet est garde dans
`docs/etape3_verification/03_prompt_llm.md`. J'ai verifie la coherence apres generation."

**Lancer :**
```bash
mysql -u root -p ArtConnect < sql/data.sql
```

**Montrer :**
```bash
mysql -u root -p ArtConnect -e "
  SELECT 'artists'  AS t, COUNT(*) FROM artists UNION ALL
  SELECT 'artworks',      COUNT(*) FROM artworks UNION ALL
  SELECT 'exhibitions',   COUNT(*) FROM exhibitions UNION ALL
  SELECT 'workshops',     COUNT(*) FROM workshops UNION ALL
  SELECT 'bookings',      COUNT(*) FROM bookings;
"
```

**Attendu :** artists=5, artworks=6, exhibitions=3, workshops=3, bookings=5.

**Eventuellement :** ouvrir `docs/etape3_verification/03_prompt_llm.md` 3 secondes
pour prouver que le prompt est bien conserve.

## Etape 3 - Objets avances (2 min)

**Dire :** "Je cree maintenant les vues, index, triggers et procedures. Au total :
3 vues, 3 index, 4 triggers, 3 procedures stockees."

**Lancer :**
```bash
mysql -u root -p ArtConnect < sql/advanced.sql
```

**Montrer l'existence des objets :**
```sql
-- dans le terminal 2 (mysql interactif)
SHOW TRIGGERS\G
SHOW PROCEDURE STATUS WHERE Db = 'ArtConnect';
SHOW FULL TABLES WHERE Table_type = 'VIEW';
SHOW INDEX FROM artworks;
```

**Dire :** "Chaque objet est documente dans `04_documentation_objets.md`."

## Etape 4 - Demo des vues (1 min)

**Dire :** "Les vues simplifient les requetes courantes."

**Lancer (terminal 2) :**
```sql
SELECT * FROM v_oeuvres_avec_artistes;
SELECT * FROM v_expositions_avec_galerie;
SELECT nom_membre, titre_atelier, statut_paiement FROM v_reservations_membres;
```

## Etape 5 - Demo des procedures (2 min)

**Dire :** "Les procedures encapsulent la logique metier."

### Stats d'un artiste
```sql
CALL sp_stats_artiste(1);
```
**Attendu :** 2 oeuvres, valeur_totale = 350000, 0 vendue, 0 en vente (toutes exposees).

### Nombre de participants
```sql
CALL sp_nb_participants(1, @n);
SELECT @n;
```
**Attendu :** `@n = 2`.

### Inscription d'un membre
```sql
CALL sp_inscrire_membre(2, 2);
SELECT * FROM bookings WHERE member_id = 2 AND workshop_id = 2;
```
**Attendu :** une nouvelle ligne dans bookings, statut PENDING.

## Etape 6 - Demo des triggers (3 min)

**Dire :** "Les triggers assurent les regles metier automatiquement, cote BD."

### Trigger 1 : dates d'exposition
```sql
-- Tentative d'exposition avec end_date < start_date
INSERT INTO exhibitions (title, start_date, end_date, gallery_id)
VALUES ('Test invalide', '2026-01-01', '2025-12-31', 1);
```
**Attendu :**
```
ERROR 1644 (45000): Erreur : la date de fin doit être après la date de début.
```

**Dire :** "Meme regle sur UPDATE :"
```sql
UPDATE exhibitions SET end_date = '2024-01-01' WHERE id = 1;
```
**Attendu :** meme erreur.

### Trigger 2 : audit des statuts d'oeuvre
```sql
-- Etat initial
SELECT * FROM audit_artworks;   -- doit etre vide

-- On modifie le statut d'une oeuvre
UPDATE artworks SET status = 'SOLD' WHERE id = 3;

-- Le trigger a logue automatiquement
SELECT * FROM audit_artworks;
```
**Attendu :** 1 ligne avec artwork_id=3, ancien_statut=FOR_SALE, nouveau_statut=SOLD.

### Trigger 3 : capacite d'atelier
```sql
-- Atelier 1 : capacite 3, deja 2 inscrits
CALL sp_inscrire_membre(3, 1);   -- doit passer (3e inscription, 3/3)
CALL sp_inscrire_membre(4, 1);   -- doit echouer : atelier plein
```
**Attendu :** la 2e appel leve `Erreur : cet atelier est complet.`

## Etape 7 - Transactions (3 min)

**Dire :** "Enfin, les scenarios transactionnels sont dans un script dedie."

**Avant de lancer** : remettre la base propre (car on vient de toucher aux bookings
et a l'audit pendant la demo) :
```bash
mysql -u root -p ArtConnect < sql/schema.sql    # recree si besoin
mysql -u root -p ArtConnect < sql/data.sql
mysql -u root -p ArtConnect < sql/advanced.sql
```

**Puis lancer le script de test :**
```bash
mysql -u root -p ArtConnect < sql/test_transactions.sql
```

**Commenter les sorties au fur et a mesure :**

- *Scenario 1* : "Deux inscriptions reussies en une transaction, COMMIT valide tout."
- *Scenario 2* : "Un doublon leve une exception dans la procedure `sp_inscrire_membre`,
  le HANDLER capture et fait un ROLLBACK. La premiere inscription, pourtant reussie,
  est aussi annulee : c'est l'atomicite."
- *Scenario 3* : "La 4e inscription fait sauter le trigger de capacite, meme mecanique
  de ROLLBACK. On prouve que trigger + transaction coopere."

## Etape 8 - Transactions cote JDBC (1 min, bonus)

**Dire :** "Cote Java, les DAOs utilisent aussi des transactions. Exemple dans
`save()` de `JdbcArtistDao` : on INSERT l'artiste, on recupere l'id genere, on
INSERT les disciplines, et on commit. Si une partie echoue, on rollback."

**Ouvrir dans VS Code :** `src/main/java/com/project/artconnect/persistence/JdbcArtistDao.java`,
montrer la methode `save()` avec le bloc `setAutoCommit(false) / commit / rollback / setAutoCommit(true)`.

## Questions probables du prof et reponses

**"Pourquoi 4 triggers au lieu de 3 ?"**
On a scinde le controle des dates d'exposition en INSERT et UPDATE pour ne pas
pouvoir contourner la regle apres creation.

**"Pourquoi les scenarios 2 et 3 sont encapsules dans une procedure ?"**
Parce qu'en MySQL, `DECLARE HANDLER` n'est valable qu'a l'interieur d'un bloc
compose (procedure, fonction, trigger). Un script .sql lineaire ne peut pas
attraper `SQLEXCEPTION`. La procedure sert uniquement de wrapper pour permettre
le HANDLER.

**"Les donnees LLM sont-elles fiables ?"**
Le prompt est conserve et je verifie systematiquement apres generation : FK
valides, UNIQUE respecte, CHECK respectes, coherence metier. Les ajustements
manuels sont listes dans `03_prompt_llm.md`.

**"Pourquoi indexer `artist_id` alors que c'est une FK ?"**
MySQL cree un index implicite sur les FK, mais je le rends explicite pour etre
sur de sa presence et pour documenter l'intention.

**"La table `audit_artworks` respecte-t-elle la 3FN ?"**
C'est une table d'historique : la redondance est intentionnelle pour conserver
l'etat passe. Il n'y a pas de dependance transitive non plus : `nouveau_statut`
depend de l'evenement (cle `id`), pas de `artwork_id`.

## Plan B si quelque chose casse

Si une commande leve une erreur inattendue :
1. Garder son calme.
2. Recharger une base propre :
   ```bash
   mysql -u root -p -e "DROP DATABASE ArtConnect;"
   mysql -u root -p < sql/schema.sql
   mysql -u root -p ArtConnect < sql/data.sql
   mysql -u root -p ArtConnect < sql/advanced.sql
   ```
3. Si MySQL refuse un `DELIMITER $$` : verifier qu'on lance bien le script avec
   `mysql < sql/advanced.sql` et pas via une interface graphique qui mange les DELIMITER.
