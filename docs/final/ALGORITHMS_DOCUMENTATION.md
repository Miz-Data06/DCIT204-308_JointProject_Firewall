# Algorithms Documentation

This document summarizes all production algorithms in `com.fooddelivery.algorithms`.

| Algorithm | What it does | Food delivery value | Input and output | Data structures used | Complexity | Trace/evidence |
|---|---|---|---|---|---|---|
| Linear search | Scans requests from first to last until a request ID is found | Finds a request in unsorted arrival-order data | Input: `CustomDynamicArray<DeliveryRequest>`, request ID. Output: `SearchResult` | `CustomDynamicArray` | O(n) time, O(n) trace space | `evidence/search_sort_demo_output.txt` shows `SR001` found in 1 operation |
| Binary search | Searches sorted request IDs by halving the range | Fast lookup once request records are sorted | Input: sorted request array, request ID. Output: `SearchResult` | `CustomDynamicArray` | O(n) validation plus O(log n) search | Evidence shows `SR450` found at index 449 |
| Selection sort | Repeatedly selects the next best request by key | Simple sorting demonstration for small batches | Input: requests and sort key. Output: `SortResult` | Arrays plus `CustomDynamicArray` result/trace | O(n^2) time, O(n) output/trace | Covered by sorting tests |
| Insertion sort | Inserts each item into the sorted prefix | Good for nearly sorted request lists | Input: requests and sort key. Output: `SortResult` | Arrays plus trace snapshots | Best O(n), average/worst O(n^2) | Covered by sorting tests |
| Merge sort | Recursively splits and merges requests | Predictable sorting for larger request batches | Input: requests and sort key. Output: `SortResult` | Arrays plus `CustomDynamicArray` trace | O(n log n) time, O(n) temp space | Covered by sorting tests |
| Quicksort | Partitions around a deterministic pivot | Fast general-purpose request ordering | Input: requests and sort key. Output: `SortResult` | Arrays plus trace snapshots | Average O(n log n), worst O(n^2) | Evidence shows first 20 requests sorted by priority |
| BFS | Visits reachable locations level by level | Shows reachable delivery zones by road hops | Input: graph and start location. Output: `TraversalResult` | `CustomQueue`, `CustomSet`, `CustomDynamicArray` | O(V + E) plus neighbor sorting | Evidence shows 150 visited from `LOC001` |
| DFS | Explores as deep as possible before backtracking | Shows branch-style exploration of service roads | Input: graph and start location. Output: `TraversalResult` | `CustomSet`, recursion, `CustomDynamicArray` | O(V + E) plus neighbor sorting | Evidence shows 150 visited from `LOC001` |
| Dijkstra fastest route | Finds the minimum effective-time path | Selects fastest route under road-condition weights | Input: graph, source, destination. Output: `RouteResult` | `CustomMap`, `CustomDynamicArray`, arrays | O(V^2 + E) with custom-array minimum scan | Evidence shows `LOC073 -> LOC076`, effective time 17.00 |
| Prim MST | Builds a minimum spanning tree/forest from a start | Finds low-cost connected service road backbone | Input: graph and start. Output: `MstResult` | `CustomSet`, `CustomDynamicArray` | O(VE) | Evidence shows total cost 2248.00 and 149 selected roads |
| Kruskal MST | Sorts roads and accepts non-cycle edges | Alternative MST using union-find | Input: graph. Output: `MstResult` | `CustomDisjointSet`, arrays, `CustomDynamicArray` | O(E^2 + E alpha(V)) due insertion-sort ordering | Evidence matches Prim total cost 2248.00 |
| Brute force request selection | Checks all subsets for best feasible selection | Exact benchmark for small request batches | Input: requests and rider capacity. Output: `RequestSelectionResult` | `CustomDynamicArray`, bit masks | O(2^n * n) | Evidence shows first 5 requests, 32 states |
| Greedy rider assignment | Selects eligible available rider with minimum pickup route time | Quick dispatch choice for one request | Input: graph, riders, request. Output: `RiderAssignmentResult` | `CustomDynamicArray`, Dijkstra | O(R * Dijkstra) | Evidence assigns rider `RES006` |
| Dynamic programming knapsack | Optimally selects requests within scaled capacity | Capacity-aware request selection | Input: requests and capacity. Output: `RequestSelectionResult` | Arrays and `CustomDynamicArray` | O(nC), C is scaled capacity | Evidence shows 5 selected requests, 5511 states |
| Index-derived priority scoring | Combines urgency, deadline, and waiting components | Creates deterministic dispatch priority | Input: normalized urgency/deadline/waiting. Output: `PriorityScoreResult` | Numeric calculation only | O(1) | Evidence shows approved weights and sample score |

## Important Implementation Rules

- Dijkstra, Prim, and Kruskal use effective travel time: `normalTravelTimeMinutes * roadConditionWeight`.
- BFS and DFS use deterministic neighbor ordering.
- Kruskal uses `CustomDisjointSet`.
- Dynamic programming capacity uses exact scaling where `1.00 = 100` integer units.
- Priority weights are approved as urgency `58 / 204`, deadline `74 / 204`, and waiting `72 / 204`.

## Result and Trace Classes

The algorithm package includes result classes such as `SearchResult`, `SortResult`, `TraversalResult`, `RouteResult`, `MstResult`, `RequestSelectionResult`, `RiderAssignmentResult`, and `PriorityScoreResult`. Step classes such as `BinarySearchStep`, `DijkstraStep`, `MstStep`, `SortTraceStep`, and `TraversalStep` make the algorithms easier to explain during the demo.
