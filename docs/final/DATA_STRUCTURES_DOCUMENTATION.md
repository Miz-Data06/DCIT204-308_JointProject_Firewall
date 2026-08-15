# Data Structures Documentation

This document summarizes the custom data structures implemented for the Food Delivery System.

| Data structure | Purpose in food delivery | Main public operations | Time complexity | Space complexity | Validation behavior | Used or useful for |
|---|---|---|---|---|---|---|
| `CustomDynamicArray` | Resizable storage for requests, locations, roads, traces, and results | `add`, `insert`, `get`, `set`, `remove`, `contains`, `indexOf`, `size`, `clear`, `iterator` | Indexed access O(1), append amortized O(1), insert/remove O(n), search O(n) | O(n) | Rejects invalid indexes and non-positive initial capacity | Core storage in algorithms, dataset loading, graph snapshots |
| `CustomLinkedList` | Node-based sequential storage | add/remove at ends and positions, get, set, search, clear | End operations generally O(1), indexed access/search O(n) | O(n) | Rejects invalid indexes and empty removals | Basis for queue/stack style structures |
| `CustomStack` | Last-in-first-out processing | `push`, `pop`, `peek`, `isEmpty`, `size`, `clear` | O(1) typical stack operations | O(n) | Rejects pop/peek on empty stack | Useful for DFS variants and request processing |
| `CustomQueue` | First-in-first-out processing | `enqueue`, `dequeue`, `peek`, `isEmpty`, `size`, `clear` | O(1) enqueue/dequeue with linked-list backing | O(n) | Rejects dequeue/peek on empty queue | BFS traversal and request queues |
| `CustomCircularQueue` | Fixed-capacity circular FIFO storage | enqueue, dequeue, peek, full/empty checks | O(1) | O(capacity) | Rejects invalid capacity, overflow, underflow | Bounded rider/request queues |
| `CustomDeque` | Double-ended queue | add/remove/peek front and rear | O(1) at both ends | O(n) | Rejects invalid empty removals/peeks | Flexible dispatch queues |
| `PriorityHeap` | Priority-based scheduling | insert, peek, extract, size, clear | Insert/extract O(log n), peek O(1) | O(n) | Rejects invalid heap operations | Useful for priority dispatch and scheduling |
| `CustomBST` | Ordered key/value indexing | put, get, contains, remove, traversal, size | Average O(log n), worst O(n) | O(n) | Rejects null or invalid keys where required | Lookup by ordered IDs or scores |
| `CustomRedBlackTree` | Balanced ordered key/value indexing | put, get, remove, contains, validation | O(log n) | O(n) | Maintains red-black properties and validates structure | Stable ordered storage under updates |
| `CustomBTree` | Multi-way ordered key/value indexing | put, get, remove/search, validation | O(log n) | O(n) | Rejects invalid degree and validates B-tree properties | Database-like indexing examples |
| `CustomHashTable` | Hash-based key/value storage | put, get, remove, containsKey, size, clear | Average O(1), worst O(n) | O(n) | Rejects null keys/values where required and resizes | Basis for map/set and graph lookup |
| `CustomMap` | Simple map abstraction | put, get, remove, containsKey, size, clear | Average O(1), worst O(n) | O(n) | Delegates key validation to hash table | Dijkstra indexes, graph vertices, CSV lookup |
| `CustomSet` | Unique-value storage | add, remove, contains, size, clear | Average O(1), worst O(n) | O(n) | Rejects invalid set entries through backing table | Visited sets in BFS/DFS/Prim |
| `AdjacencyListGraph` | Sparse weighted delivery network | add/remove vertex, add/remove edge, neighbors, weights, counts | Add/lookup average O(1), neighbor listing O(d + d^2) because results are sorted | O(V + E) | Rejects missing vertices, duplicate road IDs, duplicate endpoint pairs, invalid endpoints | Main dataset-backed graph for routing and traversal |
| `AdjacencyMatrixGraph` | Matrix-based graph alternative | add/remove vertex, add/remove edge, weights, neighbors, counts | Edge lookup O(1), vertex growth/removal can cost O(V^2) | O(V^2) | Rejects invalid vertices and edges | Dense graph demonstration and tests |
| `CustomDisjointSet` | Union-find connectivity tracking | makeSet, find, union, connected, setCount | Near O(alpha(n)) with path compression/union support | O(n) | Rejects missing/null set operations | Kruskal MST cycle detection |

## Notes

- Graph implementations store road weights using effective travel time.
- Most algorithms receive or return `CustomDynamicArray` values for traceability.
- Standard Java APIs are used for language/runtime support, dates, paths, JDBC, and tests, not as replacement project data structures.
