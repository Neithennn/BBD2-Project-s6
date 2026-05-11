# Dossier `docs/`

Documentation technique du projet ArtConnect (BBD2 T1603).

## Contenu

| Fichier | Role |
|---|---|
| `MCD.puml` | Modele Conceptuel de Donnees (Merise), PlantUML |
| `MLD.md` | Modele Logique de Donnees, notation relationnelle + explications |
| `MLD.puml` | Version visuelle du MLD (PlantUML) |
| `diagramme_classes.puml` | Diagramme de classes UML (model + service + dao + ui) |
| `diagramme_cas_usage.puml` | Diagramme de cas d'usage UML (Organisateur + Visiteur) |
| `justification_3FN.md` | Justification table par table de la 3FN |
| `documentation_objets_sql.md` | Doc des vues, index, triggers, procedures |
| `prompt_llm_data.md` | Prompt utilise pour generer `sql/data.sql` via LLM |
| `etape3_verification/` | Dossier pret pour validation par un enseignant |

## Rendre les diagrammes PlantUML

Les fichiers `.puml` sont des sources PlantUML. Trois facons de les rendre :

### 1. En ligne (aucune installation)

Copier-coller le contenu du `.puml` sur https://www.plantuml.com/plantuml/uml/
et choisir l'export PNG/SVG.

Autre site equivalent : https://planttext.com/

### 2. Extension VS Code

Installer l'extension **PlantUML** (jebbs.plantuml) et faire
`Alt+D` dans un fichier `.puml` pour la preview live.

### 3. En local via le JAR

Telecharger `plantuml.jar` depuis https://plantuml.com/download puis :
```bash
java -jar plantuml.jar docs/MCD.puml
# genere docs/MCD.png
```

Pour tout exporter :
```bash
java -jar plantuml.jar docs/*.puml
```

## Priorites

- **Etape 1 et 2** (conception/modelisation) : MCD.puml, MLD.md, MLD.puml,
  justification_3FN.md, diagramme_classes.puml, diagramme_cas_usage.puml.
- **Etape 3** (implementation + avance) : documentation_objets_sql.md,
  prompt_llm_data.md, et le sous-dossier `etape3_verification/`.
- **Etape 4** (integration JDBC) : voir directement le code dans
  `src/main/java/com/project/artconnect/persistence/` et `.../service/impl/`.
