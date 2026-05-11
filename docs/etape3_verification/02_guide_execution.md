# Guide d'execution - verification Etape 3

## Prerequis

- MySQL 8.x installe et en cours d'execution
- Acces a un compte MySQL avec droits de creation de base et de triggers
  (`SUPER` ou `CREATE ROUTINE` + `TRIGGER`)

## Etape 0 - Se placer a la racine du projet

```bash
cd BBD2-Project-s6-master
```

Tous les chemins ci-dessous sont relatifs a cette racine.

## Etape 1 - Creation de la base et des tables

```bash
mysql -u root -p < sql/schema.sql
```

Effet attendu : la base `ArtConnect` est creee avec 15 tables (dont 4 tables de
jonction et 1 table d'audit).

Verification :
```sql
USE ArtConnect;
SHOW TABLES;
```

Doit renvoyer exactement :
```
artist_disciplines
artists
artwork_artwork_tags
artwork_tags
artworks
audit_artworks
bookings
community_members
disciplines
exhibition_artworks
exhibitions
galleries
member_disciplines
reviews
workshops
```

## Etape 2 - Insertion des donnees d'exemple

```bash
mysql -u root -p ArtConnect < sql/data.sql
```

Verification rapide :
```sql
SELECT COUNT(*) FROM artists;            -- 5
SELECT COUNT(*) FROM artworks;           -- 6
SELECT COUNT(*) FROM galleries;          -- 3
SELECT COUNT(*) FROM exhibitions;        -- 3
SELECT COUNT(*) FROM workshops;          -- 3
SELECT COUNT(*) FROM community_members;  -- 5
SELECT COUNT(*) FROM bookings;           -- 5
SELECT COUNT(*) FROM reviews;            -- 5
```

## Etape 3 - Creation des objets avances

```bash
mysql -u root -p ArtConnect < sql/advanced.sql
```

Effet attendu : creation de
- 3 index (`idx_artworks_artist_id`, `idx_bookings_member_id`, `idx_artists_city`)
- 3 vues (`v_oeuvres_avec_artistes`, `v_expositions_avec_galerie`, `v_reservations_membres`)
- 4 triggers (`trg_verif_dates_exposition_insert`, `trg_verif_dates_exposition_update`, `trg_verif_capacite_workshop`, `trg_audit_statut_oeuvre`)
- 3 procedures stockees (`sp_inscrire_membre`, `sp_nb_participants`, `sp_stats_artiste`)

Verification :
```sql
-- Lister les triggers
SHOW TRIGGERS;

-- Lister les procedures
SHOW PROCEDURE STATUS WHERE Db = 'ArtConnect';

-- Lister les vues
SHOW FULL TABLES WHERE Table_type = 'VIEW';

-- Lister les index
SHOW INDEX FROM artworks;
SHOW INDEX FROM bookings;
SHOW INDEX FROM artists;
```

## Etape 4 - Execution des scenarios transactionnels

```bash
mysql -u root -p ArtConnect < sql/test_transactions.sql
```

Le script affiche l'etat initial, les 3 scenarios avec leurs resultats, puis
l'etat final. Voir `06_resultats_attendus.md` pour les sorties.

## Etape 5 - Tests manuels complementaires (optionnel)

### Test des vues
```sql
SELECT * FROM v_oeuvres_avec_artistes WHERE ville_artiste = 'Paris';
SELECT * FROM v_expositions_avec_galerie;
SELECT * FROM v_reservations_membres WHERE member_id = 1;
```

### Test des procedures
```sql
-- Inscrire un membre (doit reussir si pas de doublon, capacite ok)
CALL sp_inscrire_membre(2, 3);

-- Nombre de participants a l'atelier 1
CALL sp_nb_participants(1, @n);
SELECT @n;

-- Stats de l'artiste 1 (Leonardo Vinci)
CALL sp_stats_artiste(1);
```

### Test du trigger d'audit
```sql
UPDATE artworks SET status = 'SOLD' WHERE id = 3;
SELECT * FROM audit_artworks;
-- Doit contenir une ligne avec ancien_statut=FOR_SALE, nouveau_statut=SOLD
```

### Test du trigger de dates
```sql
-- Doit lever une erreur
INSERT INTO exhibitions (title, start_date, end_date, gallery_id)
VALUES ('Test invalide', '2026-01-01', '2025-12-31', 1);
```

### Test du trigger de capacite
```sql
-- Atelier 1 a capacite 3 et 2 inscrits. On tente 4 insertions successives :
-- la 3e passe, la 4e doit echouer.
INSERT INTO bookings (workshop_id, member_id) VALUES (1, 3);
INSERT INTO bookings (workshop_id, member_id) VALUES (1, 4);  -- doit echouer
```

## Nettoyage (optionnel)

```sql
DROP DATABASE ArtConnect;
```

Puis reprendre a l'etape 1 pour une demo rejouable.
