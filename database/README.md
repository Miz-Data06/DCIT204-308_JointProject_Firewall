# Database Schema Scripts

This folder contains the submission-facing SQLite schema for the Food Delivery System. `schema.sql` and `src/main/java/com/fooddelivery/database/DatabaseSchema.java` define the same application schema, including constraints and indexes.

## Files

| File | Purpose |
|---|---|
| `schema.sql` | SQLite-compatible DDL matching the Java initializer and JDBC repositories |

## Tables

The canonical schema preserves all six logical entities while keeping the existing Java table names and repository contracts:

| Logical entity | Table | Primary key |
|---|---|---|
| Locations | `locations` | `location_id` |
| Roads | `roads` | `road_id` |
| Requests | `delivery_requests` | `request_id` |
| Resources/riders | `riders` | `rider_id` |
| Algorithm runs | `algorithm_runs` | `run_id` |
| Audit events | `audit_events` | `audit_id` |

The tables have non-null primary keys and required fields. Constraints enforce location enum values and coordinate ranges, positive road weights and capacities, distinct road/request endpoints, rider vehicle types and boolean availability, request statuses and positive urgency, and non-negative experiment measurements with positive run numbers. Location names remain unique, preserving the existing runtime contract. Urgency accepts any positive integer, as the Java model does; CSV labels map to 1, 2 and 3. Priority scores retain the model's numeric contract without adding a non-negative restriction.

Road endpoints, rider current locations and request endpoints reference `locations.location_id`. Fourteen explicit indexes cover location names/types, road endpoints, request endpoints/status/urgency, rider location/availability, algorithm names/input sizes and audit entity/action lookups.

Request/resource CSV filenames do not have to equal their table names. The previous submission-only `service_requests` and `resources` tables are replaced by the existing application's `delivery_requests` and `riders` definitions. Road columns use `normal_travel_time_minutes` and `road_condition_weight`, matching `RoadRepository`.

## CSV-to-Database Mapping

CSV files are source datasets, not direct SQLite table dumps. Import operational records through the existing Java mappers:

| CSV field | Canonical persisted field/transformation |
|---|---|
| `roads.base_travel_time_minutes` | `roads.normal_travel_time_minutes` |
| `roads.road_condition_multiplier` | `roads.road_condition_weight` |
| `roads.road_condition` | Descriptive source label retained in CSV; the existing `Road` model stores the numeric multiplier only |
| `service_requests.urgency` | `delivery_requests.urgency`: `LOW/MEDIUM/HIGH` become `1/2/3` |
| `service_requests.status` | Java enum name: `IN_TRANSIT` becomes `PICKED_UP`; `COMPLETED` becomes `DELIVERED` |
| Request time-only fields | ISO local timestamps using the mapper's fixed date and overnight-deadline rule |
| Request capacity and calculated priority | `delivery_requests.capacity_required` and `priority_score` |
| `resources.resource_id` | `riders.rider_id`; display name is generated as `Rider {id}` |
| `resources.home_location_id` | `riders.current_location_id` |
| `resources.type` / `capacity` | `riders.vehicle_type` / `carrying_capacity` |
| `resources.availability_status` | `riders.available`: `AVAILABLE` becomes `1`; `IN_USE` and `UNDER_MAINTENANCE` become `0` |

These transformations are unchanged. The source CSVs retain the descriptive condition and three-state availability values; the schema reconciliation does not add any further loss of mapped model fields. Existing legacy CSV fallbacks are also unchanged. See `docs/final/DATABASE_AND_DATASET_DOCUMENTATION.md` for mapping details.

## Seed/Import Instructions

The normalized CSV files under `data/` remain unchanged:

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

For a fresh database, either execute `schema.sql` in SQLite or let `DatabaseInitializer` create it. Both definitions can be applied repeatedly, in either order, without changing existing canonical tables or rows.

For the Java app, run the console workflow from the project root and choose option `1` to load the dataset and import the four operational datasets into `target/food_delivery.db`:

```powershell
mvn compile exec:java "-Dexec.mainClass=com.fooddelivery.Main"
```

The application uses prepared statements and enables foreign keys on every JDBC connection. No credentials are required. `AlgorithmRunRepository` and `AuditEventRepository` support the remaining two entities; the operational dataset importer does not seed these tables or invent measurements/history.

## Compatibility Verification and Existing Databases

`SchemaCompatibilityTest` creates a database from the submitted SQL without first invoking the Java initializer, exercises all six repositories, imports and reimports the real operational dataset, compares every mapped field, and checks constraints, foreign keys and integrity. It also compares SQLite table/index definitions from both initialization paths and tests applying them in either order.

```powershell
mvn "-Dtest=SchemaCompatibilityTest" test
```

`CREATE TABLE IF NOT EXISTS` is initialization, not migration: it does not rename old columns, convert the former submission-only tables, or retrofit CHECK constraints into an existing runtime table. Existing runtime databases keep compatible column names and data, but newly added constraints require an explicit migration or a fresh database. Preserve/back up any existing database containing user data before considering migration; do not overwrite it with seed data. A database created using the old incompatible submission SQL requires explicit migration before repository use. No migration or database deletion is performed by this change.
