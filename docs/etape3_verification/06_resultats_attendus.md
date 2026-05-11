# Resultats attendus des scripts

Ce fichier donne les sorties attendues quand on execute les scripts dans l'ordre.
Il sert de "verite terrain" pour le prof : si la sortie ne correspond pas,
il y a un probleme.

## Apres `sql/schema.sql`

```
Query OK, 0 rows affected
Database ArtConnect created
...
15 CREATE TABLE executes
```

La base `ArtConnect` existe et contient 15 tables.

## Apres `sql/data.sql`

Comptes attendus par table :

| Table | Lignes attendues |
|---|---|
| disciplines | 5 |
| artists | 5 |
| artist_disciplines | 6 |
| artwork_tags | 6 |
| artworks | 6 |
| artwork_artwork_tags | 10 |
| galleries | 3 |
| exhibitions | 3 |
| exhibition_artworks | 4 |
| workshops | 3 |
| community_members | 5 |
| member_disciplines | 6 |
| bookings | 5 |
| reviews | 5 |
| audit_artworks | 0 |

## Apres `sql/advanced.sql`

`SHOW TRIGGERS` doit lister 4 entrees :
- `trg_verif_dates_exposition_insert` (BEFORE INSERT ON exhibitions)
- `trg_verif_dates_exposition_update` (BEFORE UPDATE ON exhibitions)
- `trg_verif_capacite_workshop` (BEFORE INSERT ON bookings)
- `trg_audit_statut_oeuvre` (AFTER UPDATE ON artworks)

`SHOW PROCEDURE STATUS` doit lister :
- `sp_inscrire_membre`
- `sp_nb_participants`
- `sp_stats_artiste`

`SHOW FULL TABLES WHERE Table_type = 'VIEW'` doit lister :
- `v_oeuvres_avec_artistes`
- `v_expositions_avec_galerie`
- `v_reservations_membres`

## Apres `sql/test_transactions.sql`

### Etat initial (affiche par le script)
| id | title | max_participants | nb_reservations |
|---|---|---|---|
| 1 | Maîtriser la peinture à l huile | 3 | 2 |
| 2 | Paysages impressionnistes       | 5 | 1 |
| 3 | Sculpture en argile             | 4 | 2 |

### Sortie scenario 1
- Affiche : `Scenario 1 : OK, 2 inscriptions commitees`
- `SELECT * FROM bookings WHERE member_id = 5` renvoie 2 lignes
  (workshop_id = 2 et workshop_id = 3)

### Sortie scenario 2
- Affiche : `Scenario 2 : erreur captee, ROLLBACK effectue`
- Verification : `SELECT COUNT(*) FROM bookings WHERE member_id = 3 AND workshop_id = 2` renvoie **0**

### Sortie scenario 3
- Affiche : `Scenario 3 : capacite atteinte, ROLLBACK effectue`
- Verification : `SELECT COUNT(*) FROM bookings WHERE member_id = 4 AND workshop_id = 1` renvoie **0**

### Etat final
Tables inchangees par rapport a l'etat apres scenario 1 :
- atelier 1 : 2 reservations (inchange)
- atelier 2 : 2 reservations (1 initial + 1 de scenario 1)
- atelier 3 : 3 reservations (2 initiales + 1 de scenario 1)

## Sortie attendue des tests manuels (02_guide_execution.md)

### `CALL sp_nb_participants(1, @n); SELECT @n;`
Apres le scenario 1, l'atelier 1 a toujours 2 reservations : `@n = 2`.

### `CALL sp_stats_artiste(1);`
Leonardo Vinci a 2 oeuvres (La Joconde, La Cène), toutes deux `EXHIBITED` :
- nb_oeuvres_total = 2
- valeur_totale = 350000.00
- nb_oeuvres_vendues = 0
- nb_oeuvres_en_vente = 0

### Trigger d'audit
Apres `UPDATE artworks SET status = 'SOLD' WHERE id = 3`, la table `audit_artworks`
contient une ligne :
- artwork_id = 3
- ancien_statut = `FOR_SALE`
- nouveau_statut = `SOLD`

### Trigger de dates
`INSERT INTO exhibitions (..., start_date='2026-01-01', end_date='2025-12-31', ...)`
doit echouer avec :
```
ERROR 1644 (45000): Erreur : la date de fin doit être après la date de début.
```

### Trigger de capacite
En partant d'un etat propre (post-scenario 1 donc atelier 1 toujours a 2/3) :
- INSERT (workshop_id=1, member_id=3) : passe, on est a 3/3
- INSERT (workshop_id=1, member_id=4) : echoue avec
  ```
  ERROR 1644 (45000): Erreur : cet atelier est complet.
  ```
