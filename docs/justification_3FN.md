# Justification de la normalisation (3FN)

> Projet ArtConnect - BBD2 T1603

Ce document justifie, table par table, le respect des trois premieres formes normales
pour le schema `sql/schema.sql`.

## Rappel des formes normales

**1FN - Premiere forme normale.** Un schema est en 1FN si toutes les valeurs sont
atomiques (indivisibles), et si chaque table possede une cle primaire. Pas de liste,
pas de champ multi-valeurs, pas de champ repete (discipline1, discipline2, ...).

**2FN - Deuxieme forme normale.** Un schema est en 2FN s'il est en 1FN et si chaque
attribut non-cle depend de *toute* la cle primaire, pas d'une sous-partie. Cela concerne
uniquement les tables a cle composite. Une cle primaire simple implique automatiquement
la 2FN.

**3FN - Troisieme forme normale.** Un schema est en 3FN s'il est en 2FN et s'il n'existe
aucune dependance transitive entre attributs non-cle. Autrement dit, un attribut non-cle
ne doit dependre que de la cle primaire, pas d'un autre attribut non-cle.

## Analyse table par table

### disciplines
- PK : `id`
- Attribut : `name` (unique).
- 1FN : valeur atomique.
- 2FN : cle simple, acquise.
- 3FN : aucun attribut ne depend d'un autre non-cle. OK.

### artists
- PK : `id`
- Attributs : `name`, `bio`, `birth_year`, `contact_email`, `phone`, `city`, `website`, `social_media`, `is_active`.
- 1FN : aucun champ multi-valeur (les disciplines sont externalisees via `artist_disciplines`, les oeuvres via la FK `artworks.artist_id`).
- 2FN : cle simple, acquise.
- 3FN : `city` est un simple attribut descriptif ; aucun code postal ou pays ne
  depend transitivement de la ville dans ce schema, donc on conserve `city` comme
  attribut denormalise assume (choix pedagogique pour ne pas multiplier les tables).
  Aucune autre dependance transitive. OK.

### artist_disciplines
- PK composite : `(artist_id, discipline_id)`.
- Aucun autre attribut.
- 1FN / 2FN / 3FN : table de jonction pure, trivialement en 3FN.

### artwork_tags
- PK : `id`, `name` UNIQUE.
- Meme analyse que `disciplines`. OK.

### artworks
- PK : `id`.
- Attributs : `title`, `creation_year`, `type`, `medium`, `dimensions`, `description`, `price`, `status`, `artist_id`.
- 1FN : valeurs atomiques (pas de liste de tags dans la table ; les tags sont externalises via `artwork_artwork_tags`).
- 2FN : cle simple, acquise.
- 3FN : `artist_id` est une FK ; `name` / `city` de l'artiste n'apparaissent pas ici,
  donc pas de redondance ni de dependance transitive. OK.

### artwork_artwork_tags
- PK composite : `(artwork_id, tag_id)`. Table de jonction. OK.

### galleries
- PK : `id`. Attributs descriptifs. Pas de dependance transitive. OK.

### exhibitions
- PK : `id`.
- Attributs : `title`, `start_date`, `end_date`, `description`, `gallery_id`, `curator_name`, `theme`.
- 1FN : OK.
- 2FN : cle simple.
- 3FN : les attributs de la galerie (nom, adresse) ne sont PAS dupliques ici ; on
  passe par `gallery_id`. Pas de dependance transitive. OK.

### exhibition_artworks
- PK composite. Table de jonction. OK.

### workshops
- PK : `id`.
- Attributs dont `instructor_id` (FK `artists`).
- 1FN / 2FN : OK.
- 3FN : on ne duplique pas le nom de l'instructeur ; on passe par `instructor_id`. OK.

### community_members
- PK : `id`, `email` UNIQUE.
- Attributs descriptifs (name, email, phone, city, membership_type).
- Meme remarque que `artists.city` : la ville est conservee comme attribut descriptif.
- 3FN : OK.

### member_disciplines
- PK composite. Table de jonction. OK.

### bookings
- PK : `id`.
- Attributs : `workshop_id`, `member_id`, `booking_date`, `payment_status`.
- 1FN / 2FN : OK.
- 3FN : aucune duplication du titre de l'atelier ni du nom du membre. OK.

### reviews
- PK : `id`.
- Attributs : `reviewer_id`, `artwork_id`, `rating`, `comment`, `review_date`.
- Contraintes : `UNIQUE(reviewer_id, artwork_id)`, `CHECK (rating BETWEEN 1 AND 5)`.
- 3FN : aucune redondance. OK.

### audit_artworks
- PK : `id`.
- Attributs : `artwork_id`, `ancien_statut`, `nouveau_statut`, `date_changement`.
- C'est une table d'historique ; elle conserve la trace d'evenements passes.
  Les statuts sont volontairement dupliques ici pour garder l'historique immuable,
  mais cela n'est pas une violation de la 3FN : il n'y a pas de dependance transitive,
  et la valeur `nouveau_statut` ne depend pas de `artwork_id` (elle depend de l'evenement,
  c'est-a-dire de la cle `id`).

## Choix de conception

1. **Identifiants techniques**. Chaque table principale possede un `id` INT AUTO_INCREMENT.
   Cela simplifie les FK et rend les mises a jour des champs metier (nom, titre...) sans
   impact sur les relations.

2. **Disciplines et tags externalises**. Plutot que de stocker des chaines "peinture,
   sculpture, gravure" dans `artists.disciplines`, on a cree une table `disciplines` et
   une table de jonction. Cela garantit la 1FN, evite les fautes de frappe et permet de
   renommer une discipline proprement.

3. **ON DELETE CASCADE**. Choix pragmatique : supprimer un artiste efface ses oeuvres,
   supprimer une oeuvre efface ses avis et ses lignes dans `exhibition_artworks`. On
   conserve l'integrite referentielle sans avoir besoin de scripts de nettoyage.

4. **UNIQUE sur `email` (members) et `(reviewer_id, artwork_id)` (reviews)**. Contraintes
   metier qui formalisent "un membre = un email" et "un membre ne peut laisser qu'un avis
   par oeuvre".

5. **CHECK sur `reviews.rating`**. La note est bornee a [1..5] directement au niveau
   SQL, ce qui evite de faire confiance a la couche applicative.

## Pourquoi pas de BCNF / 4FN ?

Le sujet demande explicitement la 3FN. Aller plus loin (BCNF, 4FN) n'ajouterait rien
de significatif dans ce modele : aucune dependance multivaluee problematique, et toutes
les FD non-triviales ont pour determinant une cle candidate. On est donc de facto en
BCNF sur la quasi-totalite des tables.
