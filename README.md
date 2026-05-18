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

## Index Benchmark

The `benchmark/` folder contains two Python scripts that measure the real performance gain of `idx_artists_city` on the `artists` table.

### Requirements

```powershell
pip install mysql-connector-python
```

### Usage

**1. Generate test data** (inserts 50,000 fictional artists across 100 French cities):

```powershell
python benchmark/generate_artists.py
```

**2. Run the benchmark** (measures average query time over 50 runs with and without the index, flushing the table cache between each run):

```powershell
python benchmark/benchmark_index.py
```

### Results (measured on 50,005 rows)

| | Without index | With index |
|---|---|---|
| EXPLAIN type | `ALL` (full scan ~44,000 rows) | `ref` (index lookup ~486 rows) |
| Average time | 36.6 ms | 5.6 ms |
| **Gain** | — | **6.6x faster (85% reduction)** |

The `EXPLAIN` output confirms that without the index MySQL scans the entire table, while with the index it jumps directly to matching rows.

> Note: benchmark artists are prefixed with `bench_` and are separate from the demo data. Re-running `generate_artists.py` cleans up previous benchmark rows automatically.

## Implemented Features
- Lists data from MySQL through JDBC services.
- CRUD screens for artists, artworks, and exhibitions.
- Artist-discipline relationship management.
- Artwork-artist relationship management.
- Exhibition-gallery relationship management.
- Workshop booking through stored procedure `sp_inscrire_membre`.
- UI validation for required fields, invalid dates, invalid years, negative prices, and invalid emails.
- Advanced SQL objects: views, indexes, triggers, stored procedures, and transaction test scenarios.
