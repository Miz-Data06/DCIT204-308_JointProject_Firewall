# Demo Slides Outline

PPTX generation was not available because the current Python environment does not include a presentation-generation library and no office conversion tool is installed. Use this outline to create the final oral defense slides manually.

## Slide 1: Title and Project Context

Food Delivery System for Greater Accra, Ghana. Java 17, custom data structures, algorithms, SQLite, and JDBC.

## Slide 2: Problem Modelled

The system models a food delivery service that manages locations, roads, riders, delivery requests, routing, request prioritisation, and dispatch decisions.

## Slide 3: Dataset Summary

Locations: 150. Roads: 300. Delivery requests: 900. Riders/resources: 90. Core operational records: 1,440. Additional performance/audit records: 240.

## Slide 4: System Architecture

Models represent domain entities. Custom data structures support storage and operations. Algorithms support routing, prioritisation, and assignment. JDBC imports CSV data into SQLite. The GUI/application flow demonstrates the main user experience.

## Slide 5: Custom Data Structures

Dynamic array, linked list, stack, queue, circular queue, deque, heap, hash table, map, set, trees, graphs, and disjoint set.

## Slide 6: Algorithms Implemented

Linear search, binary search, selection sort, insertion sort, merge sort, quicksort, BFS, DFS, Dijkstra, Prim, Kruskal, brute force selection, greedy rider assignment, dynamic programming knapsack, and priority scoring.

## Slide 7: Database/JDBC Integration

SQLite is used through JDBC. CSV files are parsed, mapped, validated, imported, and queried through repository classes.

## Slide 8: GUI/Application Demo Workflow

Place a delivery request, view request handling, inspect the route or result, assign a rider/resource, and show dataset-backed information. Keep the console workflow available as a technical fallback.

## Slide 9: Sample Results and Traces

Use evidence files under `evidence/`: 150 BFS/DFS visits, Dijkstra sample path, Prim/Kruskal 149 selected roads, DP selected requests, greedy assignment, and priority score output.

## Slide 10: Testing and Validation

`mvn test` passed with 513 tests. `mvn package` passed. `git diff --check` passed. Evidence outputs were captured for demos.

## Slide 11: Limitations and Future Improvements

Future work can add live traffic data, authentication, richer reporting, and real deployment packaging.

## Slide 12: Closing

The project demonstrates data structures, algorithms, database integration, testing, and a Ghana-localized food delivery workflow.
