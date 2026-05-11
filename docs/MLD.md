# MLD - Modele Logique de Donnees

> Projet ArtConnect - BBD2 T1603
> Ce document decrit le passage du MCD au schema relationnel (MLD),
> puis donne un visuel PlantUML pret a rendre.

## 1. Notation utilisee

Convention classique :
- Les cles primaires sont soulignees (ici en **gras**).
- Les cles etrangeres sont prefixees de `#` et renvoient a la table cible entre parentheses.
- Les attributs obligatoires sont marques `NOT NULL` implicitement par le schema SQL.

## 2. Schema relationnel

### Tables "principales"

**disciplines** (<u>**id**</u>, name)

**artists** (<u>**id**</u>, name, bio, birth_year, contact_email, phone, city, website, social_media, is_active)

**artwork_tags** (<u>**id**</u>, name)

**artworks** (<u>**id**</u>, title, creation_year, type, medium, dimensions, description, price, status, #artist_id => artists.id)

**galleries** (<u>**id**</u>, name, address, owner_name, opening_hours, contact_phone, rating, website)

**exhibitions** (<u>**id**</u>, title, start_date, end_date, description, curator_name, theme, #gallery_id => galleries.id)

**workshops** (<u>**id**</u>, title, date, duration_minutes, max_participants, price, location, description, level, #instructor_id => artists.id)

**community_members** (<u>**id**</u>, name, email [UNIQUE], birth_year, phone, city, membership_type)

**bookings** (<u>**id**</u>, booking_date, payment_status, #workshop_id => workshops.id, #member_id => community_members.id)

**reviews** (<u>**id**</u>, rating (1..5), comment, review_date, #reviewer_id => community_members.id, #artwork_id => artworks.id) — UNIQUE (reviewer_id, artwork_id)

**audit_artworks** (<u>**id**</u>, #artwork_id => artworks.id, ancien_statut, nouveau_statut, date_changement)

### Tables de jonction (resolution des relations N:N)

**artist_disciplines** (<u>#artist_id, #discipline_id</u>)

**artwork_artwork_tags** (<u>#artwork_id, #tag_id</u>)

**exhibition_artworks** (<u>#exhibition_id, #artwork_id</u>)

**member_disciplines** (<u>#member_id, #discipline_id</u>)

## 3. Passage MCD -> MLD

Regles appliquees :

1. Chaque **entite** devient une **table** avec une cle primaire technique `id` (AUTO_INCREMENT).
2. Chaque relation **1:N** est materialisee par une **cle etrangere** dans la table cote "N" (ex : `artwork.artist_id`, `exhibition.gallery_id`, `workshop.instructor_id`, `booking.member_id`, `review.reviewer_id`, ...).
3. Chaque relation **N:N** est resolue par une **table de jonction** dont la cle primaire est la paire des deux cles etrangeres (ex : `artist_disciplines`, `artwork_artwork_tags`, `exhibition_artworks`, `member_disciplines`).
4. Les contraintes fonctionnelles (unicite e-mail, note entre 1 et 5, unicite d'un avis par membre/oeuvre) sont materialisees par des contraintes `UNIQUE` / `CHECK`.
5. Les `ON DELETE CASCADE` sont ajoutes la ou la suppression parent doit entrainer la suppression enfant (ex : on supprime un artiste -> on supprime ses oeuvres).

## 4. Visualisation PlantUML

Le fichier `docs/MLD.puml` (voir dossier) genere un diagramme relationnel. Pour rendre :

```
# en ligne
copier-coller le contenu sur https://www.plantuml.com/plantuml/

# local
java -jar plantuml.jar docs/MLD.puml
```

## 5. Resume des dependances

```
artists         <-- artworks.artist_id
artists         <-- workshops.instructor_id
artists         <-> artist_disciplines <-> disciplines
artworks        <-> artwork_artwork_tags <-> artwork_tags
artworks        <-> exhibition_artworks <-> exhibitions
artworks        <-- reviews.artwork_id
artworks        <-- audit_artworks.artwork_id
galleries       <-- exhibitions.gallery_id
workshops       <-- bookings.workshop_id
community_members <-- bookings.member_id
community_members <-> member_disciplines <-> disciplines
community_members <-- reviews.reviewer_id
```
