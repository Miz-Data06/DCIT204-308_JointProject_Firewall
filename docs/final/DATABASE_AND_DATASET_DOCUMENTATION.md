# Database and Dataset Documentation

## CSV Files

| CSV file | Rows | Headers |
|---|---:|---|
| `locations.csv` | 150 | `location_id`, `name`, `area`, `type`, `latitude`, `longitude` |
| `roads.csv` | 300 | `road_id`, `from_location_id`, `to_location_id`, `distance_km`, `base_travel_time_minutes`, `road_condition`, `road_condition_multiplier` |
| `service_requests.csv` | 900 | `request_id`, `source_location_id`, `destination_location_id`, `category`, `urgency`, `time_submitted`, `deadline`, `status`, `capacity_required` |
| `resources.csv` | 90 | `resource_id`, `type`, `home_location_id`, `capacity`, `availability_status` |
| `algorithm_runs.csv` | 60 | `run_id`, `algorithm_name`, `input_size`, `run_number`, `runtime_ns`, `memory_kb`, `executed_at` |
| `audit_events.csv` | 0 | `audit_id`, `entity_type`, `entity_id`, `action`, `occurred_at`, `performed_by`, `details` |

## Model Mappings

| CSV | Java model | Mapping status |
|---|---|---|
| `locations.csv` | `Location` | Direct with enum-style type values |
| `roads.csv` | `Road` | Direct with official road ID and multiplier |
| `service_requests.csv` | `DeliveryRequest` | Requires urgency, time, status, capacity, and priority transforms |
| `resources.csv` | `Rider` | Requires generated rider display name |
| `algorithm_runs.csv` | `AlgorithmRunRecord` | Experiment data support |
| `audit_events.csv` | `AuditEventRecord` | Header-only support file until app-generated audit events are recorded |

## Data Transformation Rules

### Location Types

| CSV value | Enum |
|---|---|
| `Campus` | `CAMPUS` |
| `Hostel/Hall` | `HOSTEL_HALL` |
| `Junction` | `JUNCTION` |
| `Landmark` | `LANDMARK` |
| `Market` | `MARKET` |
| `Residential Area` | `RESIDENTIAL_AREA` |
| `Restaurant` | `RESTAURANT` |

### Roads

- `road_id` is preserved; legacy files without it receive stable row-order IDs such as `ROAD001`.
- `from_location_id`, `to_location_id` and `distance_km` map directly to the model and database.
- CSV `base_travel_time_minutes` maps to model normal travel time and database `normal_travel_time_minutes`.
- CSV `road_condition_multiplier` maps to model/database `road_condition_weight`, used for effective routing/MST time.
- The descriptive `road_condition` label remains in the source CSV; the existing model persists the numeric multiplier only.

### Delivery Requests

- `requestId`, `sourceLocationId`, `destinationLocationId`, and `category` map directly.
- Urgency mapping: `LOW/Low -> 1`, `MEDIUM/Medium -> 2`, `HIGH/High -> 3`.
- Time-only values use base date `2026-01-01`.
- If deadline time is not after submitted time, deadline is treated as the next day.
- Status mapping: `PENDING/Pending -> PENDING`, `ASSIGNED/Assigned -> ASSIGNED`, `IN_TRANSIT/In Transit -> PICKED_UP`, `COMPLETED/Delivered -> DELIVERED`, `CANCELLED/Cancelled -> CANCELLED`. The database stores Java enum names.
- `capacity_required` is parsed when present; missing/blank values in legacy input default to `1.0`.
- `priorityScore` is calculated deterministically using existing `PriorityScoreCalculator`.

### Riders / Resources

- `resource_id` maps to `riderId` and database `rider_id`; legacy `resource_Id` is also accepted.
- Name is generated as `Rider {id}`.
- `home_location_id` maps to `current_location_id`; legacy `homeLocation` names are resolved through the location lookup.
- Vehicle types `MOTORCYCLE`, `CAR`, and `CARGO_VEHICLE` are preserved; legacy `Motorcycle`, `Car Delivery`, and `Cargo` map to those enums.
- `capacity` maps to `carrying_capacity`.
- Availability `AVAILABLE/Available` maps to `true` (database `1`); `IN_USE/In Use` and `UNDER_MAINTENANCE/Under Maintenance` map to `false` (database `0`). The original three-state labels remain in the source CSV.

## SQLite and JDBC Choice

The local README states that the project includes JDBC database integration. No local brief or handoff specified another database engine, so SQLite was used. SQLite is appropriate here because it is file-based, needs no server setup, is easy to run in coursework demonstrations, and works with plain JDBC.

The Maven dependency is `org.xerial:sqlite-jdbc`.

## Database Tables

`database/schema.sql` and `DatabaseSchema.java` define one canonical schema. The existing application table and column names are retained, so repository public contracts and algorithm inputs are unchanged. CSV files require the transformations above rather than direct table import.

| Table | Primary key | Main fields |
|---|---|---|
| `locations` | `location_id` | name, area, type, latitude, longitude |
| `roads` | `road_id` | endpoints, distance, normal travel time, condition weight |
| `riders` | `rider_id` | name, current location, vehicle type, capacity, available |
| `delivery_requests` | `request_id` | source, destination, category, urgency, capacity, timestamps, status, priority |
| `algorithm_runs` | `run_id` | algorithm name, input size, run number, runtime, memory, execution timestamp |
| `audit_events` | `audit_id` | entity type/id, action, occurrence timestamp, actor, details |

Both initialization paths enforce non-null keys, unique location names, valid location/vehicle/request enums, coordinate ranges, positive road weights and capacities, distinct road/request endpoints, boolean rider availability, positive urgency and valid experiment measurement ranges. Urgency remains any positive integer as allowed by `DeliveryRequest`; mapped CSV labels use 1-3. Priority scores keep the existing model contract. Timestamp ordering and finite-number validation remain enforced by the Java models.

Both paths create the same fourteen explicit indexes for location, endpoint, status, availability, experiment and audit lookups. `SchemaCompatibilityTest` compares the table/index definitions and exercises SQL-created databases through the repositories and real dataset importer.

## Foreign Keys

- `roads.from_location_id` references `locations.location_id`.
- `roads.to_location_id` references `locations.location_id`.
- `riders.current_location_id` references `locations.location_id`.
- `delivery_requests.source_location_id` references `locations.location_id`.
- `delivery_requests.destination_location_id` references `locations.location_id`.

## Import Process

1. `CsvReader` reads CSV rows into `CsvRecord` objects.
2. Mapper classes convert rows into Java models.
3. `DatasetLoader` loads the core dataset into `DatasetLoadResult`.
4. `DatabaseInitializer` creates schema tables.
5. `DatasetDatabaseImporter` saves locations first, then roads, riders, and delivery requests.

Import order matters because roads, riders, and requests depend on existing locations.

`DatasetDatabaseImporter` imports only the four operational datasets. Algorithm-run and audit-event repositories remain available, but their CSV support files are not automatically imported and no synthetic measurements or operational history are generated.

## Prepared Statement Usage

Repository classes use `PreparedStatement` for insert, read, and list operations:

- `LocationRepository`
- `RoadRepository`
- `RiderRepository`
- `DeliveryRequestRepository`
- `AlgorithmRunRepository`
- `AuditEventRepository`

Schema creation uses regular JDBC `Statement` because it executes fixed SQL strings defined in `DatabaseSchema`.

## Runtime DB Path

The default runtime database path is `target/food_delivery.db`.

Generated `.db`, `.sqlite`, `.jar`, and `target/` files are not tracked because they are build/runtime artifacts. The database can be recreated from the CSV files and schema initializer.

Initialization is idempotent for the canonical schema, but is not an automatic migration of older databases. It does not convert the old submission-only table/column names or retrofit new CHECK constraints into existing tables. Preserve user data and explicitly migrate an older database when needed; see `database/README.md` for compatibility and verification instructions.
