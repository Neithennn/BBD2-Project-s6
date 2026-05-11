# ArtConnect Pro - Local Art Community Platform

## Overview
ArtConnect Pro is a JavaFX application connected to a MySQL database. It manages artists, artworks, galleries, exhibitions, workshops, bookings, and community members for a local art community.

The final application uses JDBC services by default. The old in-memory service classes are still present for reference, but `ServiceProvider` is configured to use the MySQL/JDBC implementation.

## Project Structure
- `com.project.artconnect.MainApp`: JavaFX entry point.
- `com.project.artconnect.model`: Domain entities.
- `com.project.artconnect.dao`: DAO interfaces.
- `com.project.artconnect.persistence`: JDBC DAO implementations.
- `com.project.artconnect.service`: Service interfaces.
- `com.project.artconnect.service.impl`: JDBC services and legacy in-memory services.
- `com.project.artconnect.ui`: JavaFX controllers.
- `src/main/resources/com/project/artconnect/ui`: FXML views.
- `sql/schema.sql`: Database schema, keys, relationships, and constraints.
- `sql/data.sql`: Demo data.
- `sql/advanced.sql`: Views, indexes, triggers, and stored procedures.
- `sql/test_transactions.sql`: Transaction scenarios.

## Database Setup
Default connection settings are in `DatabaseConfig`:

```text
URL: jdbc:mysql://localhost:3306/ArtConnect
USER: root
PASSWORD: root
```

They can also be overridden without editing code:

```powershell
$env:ARTCONNECT_DB_URL='jdbc:mysql://localhost:3306/ArtConnect?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Europe/Paris'
$env:ARTCONNECT_DB_USER='root'
$env:ARTCONNECT_DB_PASSWORD='root'
```

Create and fill the database:

```powershell
mysql -uroot -p < sql/schema.sql
mysql -uroot -p --default-character-set=utf8mb4 ArtConnect < sql/data.sql
mysql -uroot -p --default-character-set=utf8mb4 ArtConnect < sql/advanced.sql
```

## How to Run
Requirements: JDK 11+, Maven, MySQL 8.

```powershell
mvn clean javafx:run
```

## Verification
Compile the project:

```powershell
mvn clean test
```

Run transaction checks after loading `schema.sql`, `data.sql`, and `advanced.sql`:

```powershell
mysql -uroot -p --default-character-set=utf8mb4 ArtConnect < sql/test_transactions.sql
```

Expected database objects:
- 18 tables
- 3 views
- 4 triggers
- 3 stored procedures

## Implemented Features
- Lists data from MySQL through JDBC services.
- CRUD screens for artists, artworks, and exhibitions.
- Artist-discipline relationship management.
- Artwork-artist relationship management.
- Exhibition-gallery relationship management.
- Workshop booking through stored procedure `sp_inscrire_membre`.
- UI validation for required fields, invalid dates, invalid years, negative prices, and invalid emails.
- Advanced SQL objects: views, indexes, triggers, stored procedures, and transaction test scenarios.
