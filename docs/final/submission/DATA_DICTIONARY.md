# Data Dictionary

This dictionary documents the CSV files used by the Food Delivery System. Row counts exclude the header row.

## Summary

| File | Purpose | Rows | Mapped Java model/table |
|---|---|---:|---|
| `locations.csv` | Delivery network places such as restaurants, customers, and hubs | 150 | `Location`; `locations` |
| `roads.csv` | Road links between locations | 300 | `Road`; `roads` |
| `service_requests.csv` | Food delivery requests to prioritise, route, and dispatch | 900 | `DeliveryRequest`; `delivery_requests` |
| `resources.csv` | Rider/resource records used for assignment decisions | 90 | `Rider`; `riders` |
| `algorithm_runs.csv` | Performance measurements for implemented project algorithms | 60 | `AlgorithmRunRecord`; `algorithm_runs` |
| `audit_events.csv` | Audit event support file | 0 | `AuditEventRecord`; `audit_events` |

Core operational records total: 1,440. Official CSV row total including filtered performance and header-only audit support files: 1,500.

## `locations.csv`

Purpose: stores named locations in Greater Accra used as graph vertices and delivery endpoints.

| Column | Type/meaning | Transformation notes |
|---|---|---|
| `location_id` | String identifier such as `LOC001` | Used as the primary key and graph vertex id |
| `name` | Human-readable place name | Preserved as display text |
| `area` | Geographic area, currently Greater Accra | Preserved as dataset context |
| `type` | Location category such as restaurant or customer destination | Mapped to `LocationType` where supported |
| `latitude` | Decimal latitude | Parsed as numeric coordinate |
| `longitude` | Decimal longitude | Parsed as numeric coordinate |

Mapped class/table: `Location`; `locations`.

## `roads.csv`

Purpose: stores road connections used by routing and spanning tree algorithms.

| Column | Type/meaning | Transformation notes |
|---|---|---|
| `road_id` | Road identifier such as `ROAD001` | Used as road primary key |
| `from_location_id` | Source location id | Must match a `locations.csv` id |
| `to_location_id` | Destination location id | Must match a `locations.csv` id |
| `distance_km` | Road distance in kilometres | Parsed as decimal distance |
| `base_travel_time_minutes` | Normal travel time in minutes | Mapped to normal travel time field |
| `road_condition` | Enum-style road condition label | Preserved for documentation/database use |
| `road_condition_multiplier` | Road condition multiplier | Used with travel time to calculate effective time |

Mapped class/table: `Road`; `roads`.

Effective time transformation: `baseTravelTimeMinutes * roadConditionMultiplier`.

## `service_requests.csv`

Purpose: stores delivery requests for searching, sorting, scoring, selection, and dispatch workflows.

| Column | Type/meaning | Transformation notes |
|---|---|---|
| `request_id` | String identifier such as `SR001` | Used as request primary key |
| `source_location_id` | Source location id | Must match a location id where route-based demos need a graph point |
| `destination_location_id` | Destination location id | Must match a location id where route-based demos need a graph point |
| `category` | Request category, for example food delivery | Preserved as request category |
| `urgency` | Priority label such as low, medium, or high | Converted to an urgency score for priority scoring |
| `time_submitted` | Submission time in `HH:mm` format | Parsed for waiting-time related scoring |
| `deadline` | Deadline time | Parsed for deadline pressure scoring |
| `status` | Request status | Mapped to `RequestStatus` where supported |
| `capacity_required` | Capacity needed for the request | Deterministically set to `1.00` for food delivery requests |

Mapped class/table: `DeliveryRequest`; `delivery_requests`.

Priority scoring uses the approved derived weights: urgency `58 / 204`, deadline `74 / 204`, waiting `72 / 204`.

## `resources.csv`

Purpose: stores riders/resources used for greedy assignment and dispatch demos.

| Column | Type/meaning | Transformation notes |
|---|---|---|
| `resource_id` | Rider/resource id such as `RES001` | Mapped to rider id |
| `type` | Vehicle/resource type such as motorcycle | Mapped to `VehicleType` where supported |
| `home_location_id` | Rider base location id | Used as current/home location |
| `capacity` | Numeric carrying capacity | Parsed as numeric capacity |
| `availability_status` | Availability state | Converted to rider availability boolean/status |

Mapped class/table: `Rider`; `riders`.

## `algorithm_runs.csv`

Purpose: stores measured algorithm performance data used for final performance graphs. Rows are filtered to algorithms implemented in this project.

| Column | Type/meaning | Transformation notes |
|---|---|---|
| `run_id` | Measurement id | Preserved as run identifier |
| `algorithm_name` | Implemented project algorithm name | Filter excludes unimplemented names from the source data |
| `input_size` | Input size for the measured run | Parsed as integer |
| `run_number` | Sequential run number per algorithm | Derived from row order within each algorithm |
| `runtime_ns` | Runtime in nanoseconds | Parsed as numeric runtime |
| `memory_kb` | Memory in kilobytes | Parsed as numeric memory value |
| `executed_at` | Date and time of run | Preserved as measurement timestamp |

Mapped class/table: `AlgorithmRunRecord`; `algorithm_runs`.

## `audit_events.csv`

Purpose: provides the official audit event schema. It is header-only because the previous audit rows were not generated by the app and no operational history was invented.

| Column | Type/meaning | Transformation notes |
|---|---|---|
| `audit_id` | Audit event id | Preserved as event identifier |
| `entity_type` | Entity affected by the event | Preserved for audit filtering |
| `entity_id` | Id of the affected entity | Preserved as related record id |
| `action` | Action performed | Preserved as event action |
| `occurred_at` | Event timestamp | Preserved as event time |
| `performed_by` | System/user id | Must use `SYSTEM` or an application/user id for future rows |
| `details` | Event details | Preserved as descriptive audit text |

Mapped class/table: `AuditEventRecord`; `audit_events`.
