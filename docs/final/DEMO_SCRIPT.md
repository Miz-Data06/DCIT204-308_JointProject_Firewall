# Demo Script

This script is for a short presentation of the Food Delivery System.

## Before Running the App

Say:

> This project is a Java 17 food delivery system. The presentation demo should use the GUI/application flow to show order creation, request handling, routing, rider/resource assignment, and result output. The console workflow remains available as a technical fallback.

Then explain the main dataset:

- 150 locations
- 300 roads
- 900 delivery requests
- 90 riders/resources

## Demo Direction

Use the GUI/application experience as the main presentation path:

1. Place or request a delivery.
2. Show the route or result produced for the request.
3. Assign a rider/resource.
4. Show dataset-backed information such as locations, roads, requests, or riders.
5. Explain that custom data structures and algorithms work behind the scenes.

For a smooth live demo, use the `Use Sample Route` button, place the request, then show the delivery result cards, route visualization, assigned rider/resource, recent request table, and system summary cards.

Keep the technical explanation high level: the system uses custom structures for storing delivery data, graph algorithms for route and road-network decisions, and optimization logic for request/rider decisions.

Run the GUI with:

```powershell
mvn exec:java "-Dexec.mainClass=com.fooddelivery.gui.FoodDeliveryGuiApp"
```

## Console Fallback

If the GUI is unavailable during presentation, use Maven:

```powershell
mvn exec:java "-Dexec.mainClass=com.fooddelivery.Main"
```

If the lecturer only wants tests:

```powershell
mvn test
```

## Console Menu Flow

### Option 1 - Initialize/import dataset

Select `1`.

Expected highlight:

```text
Dataset loaded and imported to target/food_delivery.db.
Locations=150, roads=300, requests=900, riders=90
```

Explain that the program reads the CSV files, maps them into Java models, creates SQLite tables, and imports the rows using JDBC repositories.

### Option 2 - Show counts

Select `2`.

Expected highlight:

```text
Locations=150, roads=300, requests=900, riders=90
Database import: complete
```

Explain that this confirms the loaded dataset matches the expected project data.

### Option 3 - Search/sort demo

Select `3`.

Expected highlight:

```text
Search SR001 found=true, operations=1
Top sorted request=SR018
```

Explain that linear search locates a request by ID and sorting orders requests by priority.

### Option 4 - BFS/DFS traversal demo

Select `4`.

Expected highlight:

```text
BFS visited=150, start=LOC001
DFS visited=150, start=LOC001
```

Explain that BFS explores level by level, while DFS follows branches deeply before backtracking.

### Option 5 - Dijkstra fastest route demo

Select `5`.

Expected highlight:

```text
Dijkstra LOC073 -> LOC076: reachable=true, effectiveTime=6.8, pathNodes=2
```

Explain that Dijkstra uses effective travel time, not only physical distance.

### Option 6 - MST demo

Select `6`.

Expected highlight:

```text
Prim selectedRoads=149, components=1
Kruskal selectedRoads=149, components=1
```

Explain that with 150 vertices, a connected MST selects 149 roads.

### Option 7 - Optimization and priority demo

Select `7`.

Expected highlight:

```text
DP selectedRequests=5
Greedy rider assigned=true
Priority demo score=0.732843137254902
```

Explain that dynamic programming chooses requests within rider capacity, greedy assignment chooses a close eligible rider, and priority scoring combines urgency, deadline pressure, and waiting time.

### Invalid Input

Type `nope`.

Expected highlight:

```text
Invalid option. Choose 1-8.
```

Explain that the console handles invalid input safely.

### Exit

Select `8`.

Expected highlight:

```text
Goodbye.
```

## How to Explain Key Algorithms Simply

**Dijkstra:** Finds the fastest path from one location to another using effective travel time.  
**MST:** Finds a low-cost set of roads that connects all locations. Prim grows from a start location, while Kruskal sorts roads and avoids cycles using disjoint set.  
**Request optimization:** Brute force checks all combinations for small input. Dynamic programming builds the best answer step by step using capacity units.  
**Priority scoring:** Combines urgency, deadline pressure, and waiting time using the approved weights `58/204`, `74/204`, and `72/204`.

## Handling Lecturer Questions

**Why SQLite?**  
SQLite works through JDBC, needs no server setup, and stores the demo database locally under `target/`.

**Why are generated DB files not committed?**  
The database is recreated from CSV files and schema code. Generated files are build/runtime artifacts.

**Did the project use Java collection substitutes?**  
The core data structures and algorithms use the project custom structures. Standard Java APIs are used only where suitable for language features, JDBC, paths, dates, tests, and small helper tasks.

**What was the final test result?**  
519 tests passed with no failures, errors, or skipped tests.

**What is still missing?**  
The code and evidence are ready. Any extra school-specific report template or contribution declaration was not specified in provided local brief/handoff files.
