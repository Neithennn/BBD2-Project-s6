# Checklist par rapport au bareme Etape 3

Cette checklist reprend point par point les exigences du sujet et renvoie vers
les fichiers concrets du projet.

## 1. Creation et remplissage de la base

| Exigence sujet | Element fourni | Ou verifier |
|---|---|---|
| Script SQL de creation de la base | `sql/schema.sql` | 15 tables (dont 4 jonctions + 1 audit), cles primaires + cles etrangeres + contraintes NOT NULL / UNIQUE / CHECK |
| Script d'insertion de donnees d'exemple | `sql/data.sql` | Donnees realistes (artistes historiques, galeries, expositions, ateliers, membres, reservations, avis) |
| Participations croisees | `sql/data.sql` | Alice inscrite aux ateliers 1 et 2 ; atelier 3 partage par Charlie et Diana ; Nymphéas expose ET en vente |
| Prompt LLM conserve | `docs/etape3_verification/03_prompt_llm.md` | Prompt complet + verifications post-generation |

## 2. Vues, index et droits d'acces

| Exigence sujet | Objet SQL | Fichier |
|---|---|---|
| 3 vues avec objectif clair | `v_oeuvres_avec_artistes`, `v_expositions_avec_galerie`, `v_reservations_membres` | `sql/advanced.sql` (lignes 29-72) |
| 3 index justifies | `idx_artworks_artist_id`, `idx_bookings_member_id`, `idx_artists_city` | `sql/advanced.sql` (lignes 14-22) |
| Explication des choix | Chaque vue/index est documente | `docs/etape3_verification/04_documentation_objets.md` |

## 3. Declencheurs et programmes stockes

| Exigence sujet | Objet SQL | Fichier |
|---|---|---|
| 3 triggers (on en a 4) | `trg_verif_dates_exposition_insert`, `trg_verif_dates_exposition_update`, `trg_verif_capacite_workshop`, `trg_audit_statut_oeuvre` | `sql/advanced.sql` (lignes 87-149) |
| 3 procedures/fonctions stockees | `sp_inscrire_membre`, `sp_nb_participants`, `sp_stats_artiste` | `sql/advanced.sql` (lignes 163-218) |
| Controle coherence dates | Trigger `trg_verif_dates_exposition_*` | BEFORE INSERT **et** BEFORE UPDATE |
| Controle nombre de places | Trigger `trg_verif_capacite_workshop` | BEFORE INSERT ON bookings |
| Audit des modifications | Trigger `trg_audit_statut_oeuvre` | AFTER UPDATE ON artworks |
| Operation courante (inscription) | Procedure `sp_inscrire_membre` | Verifie doublon + delegue le controle capacite au trigger |
| Operation courante (participants) | Procedure `sp_nb_participants` | Retourne le nombre via parametre OUT |
| Statistiques | Procedure `sp_stats_artiste` | Nombre d'oeuvres, valeur totale, vendues, en vente |

## 4. Transactions

| Exigence sujet | Element | Fichier |
|---|---|---|
| Au moins un scenario transactionnel complexe | 3 scenarios | `sql/test_transactions.sql` |
| Scenario 1 : succes | Inscription multiple (membre 5 aux ateliers 2 et 3) -> COMMIT | lignes 23-42 |
| Scenario 2 : doublon -> ROLLBACK | HANDLER SQLEXCEPTION qui fait ROLLBACK | lignes 45-85 |
| Scenario 3 : capacite atteinte -> ROLLBACK | HANDLER qui capture le SIGNAL du trigger | lignes 88-126 |

## 5. Quantitatif minimum exige

| Categorie | Demande | Fourni |
|---|---|---|
| Triggers | >= 3 | **4** |
| Procedures/fonctions stockees | >= 3 | **3** |
| Vues | >= 3 | **3** |
| Index | >= 3 | **3** |
| Transactions complexes | >= 1 | **3 (+3 DAOs JDBC avec transactions dans `save()`)** |

## 6. Livrables explicites demandes

| Livrable demande | Emplacement |
|---|---|
| Prompt LLM | `docs/etape3_verification/03_prompt_llm.md` |
| Script d'insertion genere | `sql/data.sql` |
| Documentation triggers/procedures/vues | `docs/etape3_verification/04_documentation_objets.md` |
| Script de test des transactions | `sql/test_transactions.sql` |

Tout est reuni et prouvable avec les commandes du fichier `02_guide_execution.md`.
