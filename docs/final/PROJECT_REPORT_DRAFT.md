# Food Delivery System - Project Report Draft

## Title Page Information

**Project title:** Food Delivery System  
**Course context:** DCIT 204/308 Joint Semester Project  
**Package name:** `com.fooddelivery`  
**Main language and tools:** Java 17, Maven, JUnit 5, SQLite JDBC  
**Final implementation branch:** `feature/application-integration`  
**Final evidence commit before documentation:** `065972b docs: add demo evidence outputs`

Project brief and handoff files were not found locally apart from the README. Where a required detail was not available in the provided local materials, this report marks it as **Not specified in provided brief/handoff**.

## Project Overview

The Food Delivery System is a Java-based service operations platform for handling delivery locations, roads, riders, delivery requests, database import, and algorithm demonstrations. It combines custom data structures, graph algorithms, request optimization algorithms, CSV dataset loading, a SQLite JDBC database layer, and a console workflow.

The final system loads the dataset, imports it into SQLite, builds a custom weighted road graph, and runs demonstrations such as searching, sorting, BFS/DFS traversal, Dijkstra fastest routing, Prim and Kruskal MST, request selection, greedy rider assignment, and priority scoring.

## Problem Statement

Food delivery operations need to manage service locations, roads, requests, riders, and dispatch decisions. The problem is to design and implement a coursework-scale system that models these items, stores them, and demonstrates core data structures and algorithms using real dataset-backed examples.

## System Objectives

1. Implement custom data structures instead of using Java collection substitutes for core work.
2. Implement required searching, sorting, graph, MST, and request optimization algorithms.
3. Load and normalize the provided CSV datasets.
4. Store normalized data in a JDBC database.
5. Build a delivery road graph from real locations and roads.
6. Run dataset-backed algorithm demonstrations from a console menu.
7. Provide tests and evidence showing the system works end to end.

## Food Delivery Context

The system represents a delivery network as locations connected by roads. Roads have normal travel time and road-condition weights, so routing and network algorithms use effective travel time:

`normalTravelTimeMinutes * roadConditionWeight`

Delivery requests include source and destination locations, urgency, status, submission time, deadline, and priority. Riders have a current location, vehicle type, carrying capacity, and availability.

## Dataset Description

| File | Rows | Purpose |
|---|---:|---|
| `Locations.csv` | 150 | Delivery locations and coordinates |
| `Roads_Edges.csv` | 300 | Road connections and travel weights |
| `Service Request.csv` | 900 | Delivery request records |
| `Resource.csv` | 90 | Rider/resource records |
| `algorithm_Runs.csv` | 90 | Experiment/audit support data |
| `audit_Events.csv` | 150 | Audit support data |

The four core CSVs loaded into models are locations, roads, service requests, and resources. The two audit/experiment CSV files are documented as support data and were not required for the main console workflow.

## Data Cleaning and Mapping Decisions

- `Locations.csv` maps directly to `Location`.
- `Roads_Edges.csv` does not contain a road ID, so stable road IDs are generated as `ROAD001`, `ROAD002`, and so on.
- `distance` maps to `distanceKm`.
- `travel_Time` maps to `normalTravelTimeMinutes`.
- `roadConditionWeight` maps directly.
- `Low`, `Medium`, and `High` urgency values map to `1`, `2`, and `3`.
- Request times use the fixed synthetic date `2026-01-01`. If a deadline time is earlier than submitted time, it is treated as the next day.
- `In Transit` maps to `RequestStatus.PICKED_UP`.
- Request capacity defaults to `1.0` because the CSV does not include capacity and category values do not imply size.
- Priority score is computed deterministically using existing priority scoring and normalized urgency/deadline/waiting proxies.
- `Resource.csv` maps `resource_Id` to `riderId`.
- Rider names are generated as `Rider {resource_Id}`.
- `homeLocation` is a location name and is resolved to `locationId`.
- `Available` maps to `true`; `In Use` and `Under Maintenance` map to `false`.

## System Architecture

| Package | Responsibility |
|---|---|
| `com.fooddelivery.model` | Core domain models and enums |
| `com.fooddelivery.datastructures` | Custom linear, queue, graph, hash, heap, and tree structures |
| `com.fooddelivery.algorithms` | Searching, sorting, graph, MST, optimization, and priority algorithms |
| `com.fooddelivery.algorithms.result` | Result and trace objects |
| `com.fooddelivery.database.csv` | CSV parsing |
| `com.fooddelivery.database.mapper` | CSV-to-model mapping and dataset loading |
| `com.fooddelivery.database.repository` | JDBC repositories |
| `com.fooddelivery.database` | DB config, schema, initializer, importer |
| `com.fooddelivery.integration` | Dataset-backed algorithm workflow services |
| `com.fooddelivery.Main` | Console menu entry point |

## Models

| Model | Purpose |
|---|---|
| `Location` | Location ID, name, area, type, latitude, longitude |
| `Road` | Road ID, endpoints, distance, normal travel time, road-condition weight |
| `DeliveryRequest` | Request ID, route, category, urgency, capacity, timestamps, status, priority |
| `Rider` | Rider ID, name, current location, vehicle type, capacity, availability |
| `LocationType` | Location category enum |
| `RequestStatus` | Request lifecycle enum |
| `VehicleType` | Rider vehicle enum |

## Data Structures Implemented

The project implements `CustomDynamicArray`, `CustomLinkedList`, `CustomStack`, `CustomQueue`, `CustomCircularQueue`, `CustomDeque`, `PriorityHeap`, `CustomBST`, `CustomRedBlackTree`, `CustomBTree`, `CustomHashTable`, `CustomMap`, `CustomSet`, `AdjacencyListGraph`, `AdjacencyMatrixGraph`, and `CustomDisjointSet`.

These structures support the food delivery workflows, tests, and algorithms without depending on Java collection substitutes for core storage.

## Algorithms Implemented

The project implements linear search, binary search, selection sort, insertion sort, merge sort, quicksort, BFS, DFS, Dijkstra fastest route, Prim MST, Kruskal MST, brute force request selection, greedy rider assignment, dynamic programming knapsack/request selection, and index-derived priority scoring.

## Database and JDBC Integration

The system uses SQLite through plain JDBC. SQLite was chosen because the local README mentions JDBC integration and no local brief/handoff specified a different database engine. Runtime database output is generated under `target/food_delivery.db`. Generated database files are ignored and not committed.

The database has four main tables: `locations`, `roads`, `riders`, and `delivery_requests`. Repositories use prepared statements for inserts and queries. Foreign keys connect roads, riders, and delivery requests to locations where practical.

## Console Application Workflow

The console application provides a simple menu:

1. Initialize/import dataset
2. Show counts
3. Run search/sort demo
4. Run BFS/DFS traversal demo
5. Run Dijkstra fastest route demo
6. Run MST demo
7. Run request optimization and priority demo
8. Exit

Invalid input is handled safely. The smoke run confirmed that all menu options execute and exit cleanly.

## Testing and Validation

Final validation used `mvn test`, `mvn package`, `git diff --check`, a scripted console smoke run, and compliance scans.

Final test result:

- `511` tests run
- `0` failures
- `0` errors
- `0` skipped

## Demo Evidence Summary

Evidence files are stored in `evidence/`.

Highlights:

- Dataset counts: 150 locations, 300 roads, 900 delivery requests, 90 riders/resources
- Graph: 150 vertices, 300 edges
- Linear search `SR001`: found in 1 operation
- Binary search `SR450`: found at index 449 in 1 operation
- BFS and DFS from `LOC001`: both visited 150 locations
- Dijkstra `LOC073 -> LOC076`: reachable, path `LOC073 -> LOC076`, effective time 17.00
- Prim and Kruskal: both selected 149 roads, 1 component, total effective cost 2248.00
- Dynamic programming request selection: 5 selected requests, total priority 3.2606
- Greedy assignment: assigned rider `RES006`
- Priority scoring sample: 0.732843137254902

## Complexity Analysis Summary

| Area | Main complexity notes |
|---|---|
| Linear search | O(n) time, O(n) trace space |
| Binary search | O(log n) search after O(n) sorted validation |
| Simple sorts | Selection/insertion O(n^2) |
| Efficient sorts | Merge sort O(n log n), quicksort average O(n log n), worst O(n^2) |
| BFS/DFS | O(V + E), with extra cost for deterministic neighbor ordering |
| Dijkstra | O(V^2 + E) using custom-array minimum scan |
| Prim | O(VE) with frontier scans |
| Kruskal | O(E^2 + E alpha(V)) because edge ordering uses insertion sort |
| Brute force request selection | O(2^n * n) |
| Dynamic programming request selection | O(nC), where C is scaled capacity |

## Challenges and Solutions

| Challenge | Solution |
|---|---|
| CSV schemas did not exactly match models | Added explicit mapper classes and tests |
| Roads lacked IDs | Generated stable row-order IDs |
| Request times lacked dates | Used fixed synthetic date `2026-01-01` |
| Request priority inputs were not fully present | Used deterministic normalized proxy values and approved scoring weights |
| Rider home locations used names instead of IDs | Resolved names through loaded locations |
| Need to avoid generated DB files in Git | Runtime database writes to `target/` and DB files are ignored |
| Need to show algorithm behavior clearly | Added result/trace evidence files |

## Limitations

- The console application is intentionally simple and coursework-focused.
- Request capacity defaults to `1.0` because the CSV does not include capacity.
- The SQLite database is local and file-based, not a shared production database.
- Audit and experiment CSV files are documented but not fully integrated into the main workflow.
- Project brief and handoff details beyond README were not available locally.

## Future Improvements

- Add a richer request-size field to the dataset.
- Add report-ready exports for algorithm traces.
- Add a richer dispatch workflow with request status updates.
- Add optional filtering by area, vehicle type, or rider availability.
- Add a GUI or web front end if required later.
- Integrate `algorithm_Runs.csv` and `audit_Events.csv` into a formal audit/report module.

## Conclusion

The Food Delivery System successfully combines custom data structures, algorithms, dataset mapping, JDBC storage, integration services, and a console demonstration workflow. Final validation shows that all tests pass and the real dataset can be loaded, imported, converted into a graph, and used for algorithm demonstrations.

## Team Member Table

| Name | Student ID |
|---|---:|
| Augustine Yeboah Kumi | 22375677 |
| Emmanuel Eyram Korku Agbetor | 22206812 |
| Adu Asare Daniel | 22305775 |
| Hillary Aku Shika Allotey | 22047762 |
| Eyram Mami Araba Kumah | 22047897 |
| Nana yaw Marfo Agyei | 22397946 |
| Michael Akuffo Nyarko | 22411068 |
| Jedidiah Nii Saban Delali Annan | 22037871 |
| Sarpong Seth Appiah | 22372405 |
| Kingsbel Obese Sakyi | 22300123 |
| Musharafa Moro | 22059797 |
| Ampiah Samuel Abeka Sika | 22395143 |
| Wafaa Abdullah Layipana | 22383560 |
| Ishawu Abdul Manaf | 22406162 |
