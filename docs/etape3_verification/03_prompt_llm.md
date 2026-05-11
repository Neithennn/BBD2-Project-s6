# Prompt LLM utilise pour generer `data.sql`

> Projet ArtConnect - BBD2 T1603
> Le sujet (Etape 3) autorise explicitement l'utilisation d'un LLM pour generer
> les donnees d'exemple, a condition de conserver le prompt et de verifier le script.
> Ce document garde la trace exacte du prompt utilise ainsi que les verifications
> effectuees apres generation.

## 1. Modele et date

- **Modele :** Claude (Anthropic)
- **Contexte :** script `sql/data.sql` pour MySQL 8
- **Schema cible :** `sql/schema.sql` (fourni dans le prompt en contexte)

## 2. Prompt

```
Contexte :
Tu es en train de m'aider pour mon projet de base de donnees (BBD2 - ArtConnect).
J'ai concu un schema MySQL 8 qui respecte la 3e forme normale. Tu trouveras en piece
jointe le fichier schema.sql.

Objectif :
Genere-moi un script SQL data.sql qui remplit la base avec des donnees d'exemple
realistes et coherentes. Ce script doit me permettre de demontrer le fonctionnement
de l'application et des triggers/procedures/transactions.

Contraintes obligatoires :
1. Le script commence par USE ArtConnect;
2. Desactiver FOREIGN_KEY_CHECKS avant les TRUNCATE pour pouvoir vider les tables
   dans n'importe quel ordre, puis le reactiver.
3. Inserer les donnees dans l'ordre des dependances : disciplines, artists,
   artist_disciplines, artwork_tags, artworks, artwork_artwork_tags, galleries,
   exhibitions, exhibition_artworks, workshops, community_members, member_disciplines,
   bookings, reviews.
4. Utiliser des IDs explicites (INSERT INTO ... (id, ...) VALUES ...) pour que les
   FK soient previsibles.
5. Respecter toutes les contraintes du schema :
   - emails uniques chez community_members
   - rating reviews entre 1 et 5
   - un seul avis par (reviewer_id, artwork_id)
   - end_date > start_date pour les expositions (trigger en place)
   - max_participants coherent avec les reservations (trigger en place)

Donnees attendues :
- 5 disciplines (Peinture, Sculpture, Photographie, Art Numerique, Musique)
- 5 artistes reels et celebres (Leonardo Vinci, Monet, Ansel Adams, Frida Kahlo,
  Rodin) avec bio, email fictif, telephone fictif, ville francaise.
- 5-6 oeuvres celebres associees a leurs vrais artistes avec prix, dimensions,
  statut (FOR_SALE / SOLD / EXHIBITED).
- 6 tags (Renaissance, Impressionnisme, Portrait, Paysage, Abstrait, Noir et blanc).
- 3 galeries (Paris, Londres, New York).
- 3 expositions qui utilisent au moins 4 oeuvres, chaque expo avec un theme et un
  curateur.
- 3 ateliers avec instructor_id = un des artistes, une capacite max entre 3 et 5,
  des niveaux differents (Debutant, Intermediaire...).
- 5 membres de la communaute avec ville, phone, type d'adhesion (free/premium).
- 5 reservations qui illustrent :
    * un atelier avec plusieurs inscrits (pour tester les tests de capacite)
    * un meme membre inscrit a plusieurs ateliers (pour tester la transaction)
- 5 avis avec notes variees et commentaires en francais.

Style :
- Commentaires en francais, courts, au-dessus de chaque bloc INSERT.
- Pas de lambda ou de sous-requetes complexes : juste des INSERT simples.

Contraintes pour demo :
- L'atelier id=1 doit avoir max_participants=3 et 2 reservations existantes pour
  qu'on puisse demontrer le trigger de capacite (3e inscription passe, 4e echoue).
- Le membre id=1 doit deja etre inscrit a l'atelier id=1 pour qu'on puisse
  demontrer le controle de doublon.
- Les dates des expositions doivent etre futures par rapport a aujourd'hui.

Rends-moi uniquement le contenu du fichier data.sql.
```

## 3. Verifications effectuees apres generation

Verifications systematiques passees en revue avant commit :

1. **Coherence FK.** Chaque `artist_id`, `gallery_id`, `workshop_id`, `member_id`,
   `discipline_id`, `tag_id` reference bien un id existant dans la table parent.
   Verifie manuellement en relisant les INSERTs.

2. **Contraintes UNIQUE.** Tous les emails de `community_members` sont distincts.
   Chaque paire `(reviewer_id, artwork_id)` de `reviews` est unique.

3. **Contraintes CHECK.** Toutes les notes (`rating`) sont dans `[1..5]`.

4. **Contraintes metier.**
   - Aucune exposition n'a `end_date <= start_date` (le trigger aurait sinon bloque).
   - Les ateliers ont un `instructor_id` qui pointe bien vers un artiste.
   - Les capacites `max_participants` sont superieures ou egales au nombre
     de reservations deja creees dans `bookings`.

5. **Realisme.** Les artistes et oeuvres correspondent a la realite historique
   (Leonardo a bien peint la Joconde, Rodin a bien sculpte le Penseur, etc.).

6. **Execution.** Le script se deroule sans erreur avec :
   ```
   mysql -u root -p ArtConnect < sql/data.sql
   ```

7. **Preparation des scenarios de test.**
   - Atelier 1 : capacite=3, 2 inscrits -> on peut ajouter 1 puis declencher le trigger.
   - Membre 1 inscrit deja a l'atelier 1 -> on peut declencher `sp_inscrire_membre`
     en erreur de doublon.

## 4. Ce qui a ete corrige a la main apres generation

Quelques ajustements mineurs apportes au script genere :

- Ajout explicite des ids dans les INSERTs (le LLM avait omis ca sur 2 blocs).
- Reecriture de quelques accents / apostrophes qui passaient mal en UTF-8 dans le
  client MySQL (utilisation de `l oeuvre` au lieu de `l'oeuvre` pour eviter les
  problemes de quoting).
- Re-ordonnancement des TRUNCATE pour eviter les warnings sur les FK.

Toutes ces modifications ont ete faites manuellement et non regenerees par le LLM,
pour garder le controle sur le contenu final.
