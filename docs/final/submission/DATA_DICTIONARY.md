# Data Dictionary

This dictionary documents the CSV files used by the Food Delivery System. Row counts exclude the header row.

## Summary

| File | Purpose | Rows | Mapped Java model/table |
|---|---|---:|---|
| `Locations.csv` | Delivery network places such as restaurants, customers, and hubs | 150 | `Location`; `locations` |
| `Roads_Edges.csv` | Road links between locations | 300 | `Road`; `roads` |
| `Service Request.csv` | Food delivery requests to prioritise, route, and dispatch | 900 | `DeliveryRequest`; `delivery_requests` |
| `Resource.csv` | Rider/resource records used for assignment decisions | 90 | `Rider`; `riders` |
| `algorithm_Runs.csv` | Performance measurements for algorithms | 90 | Experiment/evidence support |
| `audit_Events.csv` | Audit history for operational events | 150 | Audit/evidence support |

Core operational records total: 1,440. Grand CSV row total including performance and audit support files: 1,680.

## `Locations.csv`

Purpose: stores named locations in Greater Accra used as graph vertices and delivery endpoints.

| Column | Type/meaning | Transformation notes |
|---|---|---|
| `locationId` | String identifier such as `LOC001` | Used as the primary key and graph vertex id |
| `name` | Human-readable place name | Preserved as display text |
| `area` | Geographic area, currently Greater Accra | Preserved as dataset context |
| `type` | Location category such as restaurant or customer destination | Mapped to `LocationType` where supported |
| `latitude` | Decimal latitude | Parsed as numeric coordinate |
| `longitude` | Decimal longitude | Parsed as numeric coordinate |

Mapped class/table: `Location`; `locations`.

## `Roads_Edges.csv`

Purpose: stores road connections used by routing and spanning tree algorithms.

| Column | Type/meaning | Transformation notes |
|---|---|---|
| `fromLocationId` | Source location id | Must match a `Locations.csv` id |
| `toLocationId` | Destination location id | Must match a `Locations.csv` id |
| `distance` | Road distance in kilometres | Parsed as decimal distance |
| `travel_Time` | Normal travel time in minutes | Mapped to normal travel time field |
| `roadConditionWeight` | Road condition multiplier | Used with travel time to calculate effective time |

Mapped class/table: `Road`; `roads`.

Effective time transformation: `normalTravelTimeMinutes * roadConditionWeight`.

## `Service Request.csv`

Purpose: stores delivery requests for searching, sorting, scoring, selection, and dispatch workflows.

| Column | Type/meaning | Transformation notes |
|---|---|---|
| `requestId` | String identifier such as `SR001` | Used as request primary key |
| `source` | Source place name | Preserved for display |
| `sourceLocationId` | Source location id | Must match a location id where route-based demos need a graph point |
| `destination` | Destination place name | Preserved for display |
| `destinationLocationId` | Destination location id | Must match a location id where route-based demos need a graph point |
| `category` | Request category, for example food delivery | Preserved as request category |
| `urgency` | Priority label such as low, medium, or high | Converted to an urgency score for priority scoring |
| `timeSubmitted` | Submission time | Parsed for waiting-time related scoring |
| `deadline` | Deadline time | Parsed for deadline pressure scoring |
| `status` | Request status | Mapped to `RequestStatus` where supported |

Mapped class/table: `DeliveryRequest`; `delivery_requests`.

Priority scoring uses the approved derived weights: urgency `58 / 204`, deadline `74 / 204`, waiting `72 / 204`.

## `Resource.csv`

Purpose: stores riders/resources used for greedy assignment and dispatch demos.

| Column | Type/meaning | Transformation notes |
|---|---|---|
| `resource_Id` | Rider/resource id such as `RES001` | Mapped to rider id |
| `type` | Vehicle/resource type such as motorcycle | Mapped to `VehicleType` where supported |
| `homeLocation` | Rider base location name | Preserved for assignment display and matching |
| `capacity` | Numeric carrying capacity | Parsed as numeric capacity |
| `available_Status` | Availability state | Converted to rider availability boolean/status |

Mapped class/table: `Rider`; `riders`.

## `algorithm_Runs.csv`

Purpose: stores measured algorithm performance data used for final performance graphs.

| Column | Type/meaning | Transformation notes |
|---|---|---|
| `runId` | Measurement id | Preserved as run identifier |
| `algoritmName` | Algorithm name from the performance dataset | Header spelling is preserved from the source CSV |
| `inputSize` | Input size for the measured run | Parsed as integer |
| `time(Ns)` | Runtime in nanoseconds | Parsed as numeric runtime |
| `memory(Kb)` | Memory in kilobytes | Parsed as numeric memory value |
| `Date run` | Date and time of run | Preserved as measurement timestamp |

Mapped class/table: performance evidence support, not a core operational model.

## `audit_Events.csv`

Purpose: stores audit-style events that support traceability and demonstration context.

| Column | Type/meaning | Transformation notes |
|---|---|---|
| `auditId` | Audit event id | Preserved as event identifier |
| `entityType` | Entity affected by the event | Preserved for audit filtering |
| `entityId` | Id of the affected entity | Preserved as related record id |
| `action` | Action performed | Preserved as event action |
| `Timestamp` | Event timestamp | Preserved as event time |
| `performedBy` | Person or role recorded in the source data | Preserved from the dataset |
| `details` | Event details | Preserved as descriptive audit text |

Mapped class/table: audit evidence support, not a core operational model.
