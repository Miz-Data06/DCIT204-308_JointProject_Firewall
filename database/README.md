# Database Schema Scripts

This folder contains the official submission-facing SQLite schema for the Food Delivery System.

## Files

| File | Purpose |
|---|---|
| `schema.sql` | SQLite-compatible DDL for the normalized project tables |

## Tables

The official schema defines:

- `locations`
- `roads`
- `service_requests`
- `resources`
- `algorithm_runs`
- `audit_events`

The Java application also maintains a runtime schema in `src/main/java/com/fooddelivery/database/DatabaseSchema.java`. The runtime schema is compatible with the application model classes and uses prepared-statement repositories. This SQL script is the normalized submission schema requested by the project brief.

## Seed/Import Instructions

Use the official normalized CSV files under `data/`:

- `locations.csv`
- `roads.csv`
- `service_requests.csv`
- `resources.csv`
- `algorithm_runs.csv`
- `audit_events.csv`

SQLite foreign key enforcement must be enabled before inserts:

```sql
PRAGMA foreign_keys = ON;
```

For the Java app, run the console workflow and choose option `1` to load the dataset and import the operational records into the runtime SQLite database at `target/food_delivery.db`.

```powershell
mvn clean package
java -jar target/food-delivery-system-1.0-SNAPSHOT.jar
```

No credentials are required because the project uses local SQLite through JDBC.
