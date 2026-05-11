# Documentation des objets SQL avances

> Projet ArtConnect - BBD2 T1603
> Fichier de reference : `sql/advanced.sql` et `sql/test_transactions.sql`

Le sujet (Etape 3, 30% de la note) demande *au moins* trois objets de chaque type :
triggers, procedures stockees, vues, et index.

## 1. Index

Trois index ont ete cres pour accelerer les recherches frequentes.

| Index | Table cible | Colonne(s) | Justification |
|---|---|---|---|
| `idx_artworks_artist_id` | `artworks` | `artist_id` | Tres utilise pour `SELECT * FROM artworks WHERE artist_id = ?`. |
| `idx_bookings_member_id` | `bookings` | `member_id` | Afficher les reservations d'un membre (`findByMember`). |
| `idx_artists_city`       | `artists`  | `city`       | Recherche d'artistes par ville dans `DiscoverController`. |

Les colonnes FK sont deja indexees implicitement par MySQL, donc on ajoute un index
explicite uniquement sur les colonnes non-FK (ou l'on force l'index quand on veut etre sur).

## 2. Vues

Trois vues ont ete creees pour factoriser les jointures et simplifier la lecture.

### v_oeuvres_avec_artistes
Joint `artworks` avec `artists`. Colonnes : `artwork_id, titre, type_oeuvre, statut, prix, artist_id, nom_artiste, ville_artiste`.

Utilite : lister les oeuvres avec leur auteur sans refaire le JOIN partout.

```sql
SELECT * FROM v_oeuvres_avec_artistes WHERE ville_artiste = 'Paris';
```

### v_expositions_avec_galerie
Joint `exhibitions` avec `galleries`. Colonnes : `exposition_id, titre_expo, date_debut, date_fin, theme, gallery_id, nom_galerie, adresse_galerie`.

Utilite : afficher les expositions avec leur galerie dans la UI.

### v_reservations_membres
Triple jointure entre `bookings`, `community_members` et `workshops`. Colonnes : `booking_id, date_reservation, statut_paiement, member_id, nom_membre, email_membre, workshop_id, titre_atelier, date_atelier, places_max`.

Utilite : preparer les emails de confirmation, afficher les reservations d'un membre.

## 3. Triggers

Quatre triggers au total (on depasse le minimum de 3).

### trg_verif_dates_exposition_insert (BEFORE INSERT ON exhibitions)
Empeche la creation d'une exposition dont `end_date <= start_date`.

### trg_verif_dates_exposition_update (BEFORE UPDATE ON exhibitions)
Meme regle sur un `UPDATE` : on ne peut pas contourner la contrainte apres coup.

### trg_verif_capacite_workshop (BEFORE INSERT ON bookings)
Avant d'inserer une nouvelle reservation, compte les reservations deja existantes
pour l'atelier et compare a `workshops.max_participants`. Si l'atelier est plein,
leve `SIGNAL SQLSTATE '45000'` avec le message *"cet atelier est complet."*.

Ce trigger travaille en cooperation avec la procedure `sp_inscrire_membre` : la
procedure verifie l'unicite de l'inscription, le trigger verifie la capacite.

### trg_audit_statut_oeuvre (AFTER UPDATE ON artworks)
Si le statut d'une oeuvre change (`OLD.status != NEW.status`), insere une ligne dans
`audit_artworks` avec l'ancien et le nouveau statut. Permet de garder un historique
des changements (utile pour le suivi commercial : vente, retrait de vente...).

## 4. Procedures stockees

### sp_inscrire_membre(IN p_member_id INT, IN p_workshop_id INT)
Inscrit un membre a un atelier. Verifie d'abord que le membre n'est pas deja inscrit
(leve `SQLSTATE 45000` sinon), puis fait l'`INSERT` dans `bookings`. Le trigger
`trg_verif_capacite_workshop` se declenche automatiquement pour verifier la capacite.

Utilisation :
```sql
CALL sp_inscrire_membre(5, 2);
```

### sp_nb_participants(IN p_workshop_id INT, OUT p_nb_participants INT)
Retourne dans un parametre `OUT` le nombre de participants effectifs a un atelier
(on exclut les reservations `CANCELLED`).

Utilisation :
```sql
CALL sp_nb_participants(1, @nb);
SELECT @nb;
```

### sp_stats_artiste(IN p_artist_id INT)
Retourne un jeu de resultats avec :
- nombre total d'oeuvres de l'artiste
- valeur totale (somme des prix)
- nombre d'oeuvres vendues
- nombre d'oeuvres a vendre

Utilisation :
```sql
CALL sp_stats_artiste(2);
```

## 5. Transactions

Le script `sql/test_transactions.sql` met en scene 3 scenarios transactionnels.

### Scenario 1 : inscriptions multiples reussies (COMMIT)
```sql
START TRANSACTION;
CALL sp_inscrire_membre(5, 2);
CALL sp_inscrire_membre(5, 3);
COMMIT;
```
Si toutes les inscriptions passent, on valide le tout en une fois.

### Scenario 2 : doublon detecte (ROLLBACK par HANDLER)
On wrap les appels dans une procedure `test_scenario_2` qui contient :
```sql
DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
    ROLLBACK;
END;
```
Une premiere inscription reussit, la seconde tente de reinscrire un membre deja
inscrit -> `sp_inscrire_membre` leve un `SIGNAL` -> le HANDLER fait `ROLLBACK` ->
la premiere inscription est elle aussi annulee. La table `bookings` n'est pas modifiee.

### Scenario 3 : capacite atteinte (ROLLBACK par HANDLER)
Meme mecanique : on remplit l'atelier, puis on essaie une inscription de trop. Le
trigger `trg_verif_capacite_workshop` leve l'exception, le HANDLER fait `ROLLBACK`.

Ce scenario illustre la cooperation entre la contrainte SQL (trigger) et la gestion
transactionnelle cote client (HANDLER + ROLLBACK).

## 6. Cote Java - transactions JDBC

La couche DAO (`JdbcArtistDao`, `JdbcArtworkDao`, `JdbcExhibitionDao`) implemente des
transactions dans les methodes `save()` pour les inserts multi-tables :

```java
conn.setAutoCommit(false);
try {
    // INSERT dans la table principale
    // ... + recuperation de la cle generee
    // INSERT dans les tables de jonction (artist_disciplines, artwork_artwork_tags...)
    conn.commit();
} catch (SQLException e) {
    conn.rollback();
    throw e;
} finally {
    conn.setAutoCommit(true);
}
```

Cela garantit que si l'insertion des disciplines/tags echoue, l'artiste/oeuvre principale
n'est pas persistee non plus : on ne se retrouve jamais avec un etat partiel.

## 7. Recapitulatif quantitatif

| Exigence | Demande | Fourni |
|---|---|---|
| Triggers            | 3 | **4** |
| Procedures stockees | 3 | **3** |
| Vues                | 3 | **3** |
| Index               | 3 | **3** |
| Transactions        | 1 scenario | **3 scenarios SQL + 3 DAOs JDBC** |
