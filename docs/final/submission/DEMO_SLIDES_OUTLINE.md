# Demo Slides Outline

PPTX generation was not available because the current Python environment does not include a presentation-generation library and no office conversion tool is installed. Use this outline to create the final oral defense slides manually.

## Slide 1: Title and Project Context

Food Delivery System for Greater Accra, Ghana. Java 17, custom data structures, algorithms, SQLite, and JDBC.

## Slide 2: Problem Modelled

The system models a food delivery service that manages locations, roads, riders, delivery requests, routing, request prioritisation, and dispatch decisions.

## Slide 3: Dataset Summary

Locations: 150. Roads: 300. Delivery requests: 900. Riders/resources: 90. Core operational records: 1,440. Additional performance/audit records: 240.

## Slide 4: System Architecture

Models represent domain entities. Custom data structures support storage and operations. Algorithms perform search, sorting, graph routing, MST, priority scoring, and optimisation. JDBC imports CSV data into SQLite. The console menu demonstrates the workflows.

## Slide 5: Custom Data Structures

Dynamic array, linked list, stack, queue, circular queue, deque, heap, hash table, map, set, trees, graphs, and disjoint set.

## Slide 6: Algorithms Implemented

Linear search, binary search, selection sort, insertion sort, merge sort, quicksort, BFS, DFS, Dijkstra, Prim, Kruskal, brute force selection, greedy rider assignment, dynamic programming knapsack, and priority scoring.

## Slide 7: Database/JDBC Integration

SQLite is used through JDBC. CSV files are parsed, mapped, validated, imported, and queried through repository classes.

## Slide 8: Console Demo Workflow

Load dataset and database, run search/sort demos, run traversal/routing demos, run MST demos, run optimisation demos, run priority scoring demo, and exit cleanly.

## Slide 9: Sample Results and Traces

Use evidence files under `evidence/`: 150 BFS/DFS visits, Dijkstra sample path, Prim/Kruskal 149 selected roads, DP selected requests, greedy assignment, and priority score output.

## Slide 10: Testing and Validation

`mvn test` passed with 511 tests. `mvn package` passed. `git diff --check` passed. Evidence outputs were captured for demos.

## Slide 11: Limitations and Future Improvements

The project is a console-based academic prototype. Future work can add a GUI, live traffic data, authentication, richer reporting, and real deployment packaging.

## Slide 12: Closing

The project demonstrates data structures, algorithms, database integration, testing, and a Ghana-localized food delivery workflow.
