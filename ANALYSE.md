# Analyse Fonctionnelle — ArtConnect (BBD2 T1603)

## 1. Présentation de l'application

ArtConnect est une plateforme qui met en relation des artistes et des amateurs d'art.
Elle permet de gérer des œuvres, des galeries, des expositions, des ateliers et
une communauté de membres.

---

## 2. Fonctionnalités principales

### Gestion des artistes
- Créer, modifier et supprimer un profil d'artiste
- Associer un artiste à une ou plusieurs disciplines artistiques
- Rechercher des artistes par nom, ville ou discipline

### Gestion des œuvres
- Ajouter une œuvre et l'associer à son artiste
- Définir le statut d'une œuvre (À vendre, Vendue, Exposée)
- Associer des tags (mots-clés) à une œuvre

### Gestion des galeries et expositions
- Enregistrer des galeries avec leurs coordonnées
- Créer des expositions dans une galerie (avec dates de début et de fin)
- Ajouter des œuvres à une exposition

### Gestion des ateliers
- Créer des ateliers animés par un artiste
- Définir une capacité maximale de participants
- Permettre aux membres de s'inscrire à un atelier

### Gestion de la communauté
- Inscrire des membres (type : gratuit ou premium)
- Permettre aux membres de laisser des avis sur les œuvres
- Consulter les réservations d'un membre

---

## 3. Définition des rôles

### Rôle : Organisateur
L'organisateur est responsable de la gestion du contenu de la plateforme.

**Droits :**
- Créer, modifier et supprimer des artistes
- Créer, modifier et supprimer des œuvres
- Créer et gérer des galeries et des expositions
- Créer et gérer des ateliers
- Consulter toutes les réservations et les avis

### Rôle : Visiteur
Le visiteur est un membre de la communauté qui consulte et interagit avec le contenu.

**Droits :**
- Consulter les artistes, œuvres, galeries et expositions
- S'inscrire à des ateliers
- Laisser un avis (note + commentaire) sur une œuvre

---

## 4. Entités et relations

### Liste des entités
| Entité | Description |
|---|---|
| Artist | Un artiste avec son profil complet |
| Artwork | Une œuvre créée par un artiste |
| Discipline | Un domaine artistique (peinture, sculpture...) |
| ArtworkTag | Un mot-clé associé à une œuvre |
| Gallery | Une galerie qui accueille des expositions |
| Exhibition | Une exposition organisée dans une galerie |
| Workshop | Un atelier animé par un artiste |
| CommunityMember | Un membre de la communauté |
| Booking | Une réservation d'un membre pour un atelier |
| Review | Un avis d'un membre sur une œuvre |

### Relations entre entités

```
Artist ──< Artwork            Un artiste crée plusieurs œuvres (1:N)
Artist >──< Discipline        Un artiste pratique plusieurs disciplines (N:N)
Artwork >──< ArtworkTag       Une œuvre a plusieurs tags (N:N)
Gallery ──< Exhibition        Une galerie accueille plusieurs expositions (1:N)
Exhibition >──< Artwork       Une exposition présente plusieurs œuvres (N:N)
Artist ──< Workshop           Un artiste anime plusieurs ateliers (1:N)
Workshop ──< Booking          Un atelier a plusieurs réservations (1:N)
CommunityMember ──< Booking   Un membre fait plusieurs réservations (1:N)
CommunityMember >──< Discipline  Un membre a des disciplines favorites (N:N)
CommunityMember ──< Review    Un membre laisse plusieurs avis (1:N)
Artwork ──< Review            Une œuvre reçoit plusieurs avis (1:N)
```

### Justification de la 3e Forme Normale (3FN)

La base de données respecte la 3FN car :
1. **1FN** : Toutes les valeurs sont atomiques (pas de listes dans les colonnes).
   Les relations N:N (disciplines, tags) sont résolues par des tables de jonction.
2. **2FN** : Chaque attribut non-clé dépend entièrement de la clé primaire
   (les clés primaires sont simples, sauf dans les tables de jonction).
3. **3FN** : Il n'y a pas de dépendance transitive. Par exemple, la discipline
   d'un artiste n'est pas stockée dans la table `artists` mais dans une table
   séparée `disciplines` reliée par `artist_disciplines`.
