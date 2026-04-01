# Instructions du Projet ArtConnect - Bases de Données 2 (T1603)

## 1. Contexte du Projet
[cite_start]Ce projet consiste à concevoir une base de données relationnelle MySQL pour une application Java existante nommée ArtConnect[cite: 4, 5]. [cite_start]L'application permet de mettre en relation des artistes, présenter leurs œuvres, organiser des événements et permettre l'inscription des membres[cite: 9, 10, 11, 12]. [cite_start]Actuellement, l'application fonctionne uniquement avec des données en mémoire[cite: 14].

## 2. Consignes de Style (Règle d'Or)
IMPORTANT : Tu dois agir comme un étudiant en informatique, pas comme un développeur senior.
- [cite_start]**Code Simple :** Utilise des structures de contrôle classiques (if-else sur plusieurs lignes avec accolades)[cite: 109].
- **Lisibilité :** Évite les syntaxes trop modernes ou complexes comme les Streams Java avancés ou les expressions lambdas imbriquées.
- **Commentaires :** Écris des commentaires en français, courts et concis pour expliquer les étapes clés (ex: // Connexion à la base de données).
- [cite_start]**Architecture :** Respecte strictement l'architecture fournie (Entities -> DAO -> Services)[cite: 109, 142].

## 3. Étapes à Réaliser (Barème de Notation)

### [cite_start]Étape 1 & 2 : Conception et Modélisation (40% de la note) [cite: 150]
- [cite_start]**Analyse fonctionnelle :** Identifier les entités et définir deux rôles : "Organisateur" et "Visiteur"[cite: 40, 43].
- [cite_start]**Normalisation :** Concevoir une base de données respectant strictement la **3e Forme Normale (3FN)**[cite: 57, 69].
- [cite_start]**Script SQL :** Créer un fichier `schema.sql` contenant les `CREATE TABLE` avec clés primaires, clés étrangères et contraintes `NOT NULL`[cite: 71, 72].

### [cite_start]Étape 3 : Fonctionnalités SQL Avancées (30% de la note) [cite: 150]
[cite_start]Tu dois créer au moins **trois objets de chaque type** suivant[cite: 94]:
- [cite_start]**3 Triggers :** Pour l'automatisation (ex: contrôle de cohérence des dates, contrôle du nombre de places, audit des modifications)[cite: 90].
- [cite_start]**3 Procédures/Fonctions stockées :** Pour les opérations courantes (ex: inscription automatique d'un artiste à un événement, calcul du nombre de participants)[cite: 91].
- [cite_start]**3 Vues et 3 Index :** Pour simplifier les requêtes et optimiser les recherches[cite: 88, 89].
- [cite_start]**Transactions :** Implémenter au moins un scénario transactionnel complexe (ex: inscription d'un membre à plusieurs événements de manière atomique)[cite: 93].
- [cite_start]**Données :** Générer un script d'insertion de données réalistes[cite: 85, 86].

### [cite_start]Étape 4 : Intégration Java JDBC (20% de la note) [cite: 150]
- [cite_start]**DAO :** Implémenter les interfaces DAO dans le package `org.project.artconnect.dao.impl`[cite: 104].
- [cite_start]**Persistance :** Créer les classes JDBC (ex: `JdbcArtistDao`) dans `org.project.artconnect.persistence`[cite: 105].
- [cite_start]**JDBC :** Utiliser des `PreparedStatement`, gérer les transactions si nécessaire et assurer la fermeture correcte des ressources[cite: 106].
- [cite_start]**Configuration :** Compléter `DatabaseConfig` pour la connexion vers la base `ArtConnect`[cite: 113].

## 4. Livrables Attendus
- [cite_start]Script SQL complet (schéma + données + fonctions avancées)[cite: 27, 77, 95].
- [cite_start]Code source Java intégré et fonctionnel sur GitHub[cite: 28, 119].
- [cite_start]Documentation technique expliquant les choix de conception et la normalisation[cite: 76, 131, 139].