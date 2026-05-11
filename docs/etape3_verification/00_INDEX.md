# Etape 3 - Dossier de verification

> Projet ArtConnect - BBD2 T1603
> Ce dossier regroupe tout le necessaire pour qu'un enseignant puisse verifier
> l'Etape 3 (Implementation BD + fonctionnalites avancees) en suivant un ordre
> reproductible.

## Contenu

| Fichier | Contenu |
|---|---|
| `00_INDEX.md` | Ce fichier, guide d'utilisation du dossier |
| `01_checklist_bareme.md` | Checklist point par point du bareme Etape 3 |
| `02_guide_execution.md` | Comment lancer les scripts pour tout verifier |
| `03_prompt_llm.md` | Prompt LLM utilise pour generer `data.sql` |
| `04_documentation_objets.md` | Doc des vues, index, triggers, procedures |
| `05_scenario_transactions.md` | Explication pas a pas du script de test |
| `06_resultats_attendus.md` | Sorties attendues des scripts de test |
| `07_script_demo_prof.md` | Script pas-a-pas pour la demo live devant le prof |

## Scripts SQL a la racine du projet

| Fichier | Role |
|---|---|
| `sql/schema.sql` | Creation de la base et des 15 tables |
| `sql/advanced.sql` | Vues, index, triggers, procedures stockees |
| `sql/data.sql` | Donnees d'exemple generees via LLM |
| `sql/test_transactions.sql` | 3 scenarios transactionnels |

## Ordre recommande pour la verification

1. Lire `01_checklist_bareme.md` pour voir ce qui doit etre verifie.
2. Lire `02_guide_execution.md` et executer les 4 scripts dans l'ordre.
3. Confronter les sorties obtenues avec `06_resultats_attendus.md`.
4. Consulter `04_documentation_objets.md` pour comprendre chaque objet avance.
5. Lire `05_scenario_transactions.md` pour le detail des 3 scenarios.
6. Lire `03_prompt_llm.md` pour la trace de generation des donnees.
