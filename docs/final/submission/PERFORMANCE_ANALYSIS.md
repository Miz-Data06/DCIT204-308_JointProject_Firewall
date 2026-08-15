# Performance Analysis

The performance checklist uses the existing file `data/algorithm_Runs.csv`. The CSV contains 90 measured runs with these fields: run id, algorithm name, input size, runtime in nanoseconds, memory in kilobytes, and date run. The source header `algoritmName` is preserved as written in the dataset.

## Generated Graphs

| Graph | File | What it shows |
|---|---|---|
| Runtime by algorithm/input size | `docs/final/submission/performance_graphs/runtime_by_algorithm.png` | Runtime trend for each algorithm across measured input sizes |
| Memory by algorithm/input size | `docs/final/submission/performance_graphs/memory_by_algorithm.png` | Memory trend for each algorithm across measured input sizes |
| Average runtime per algorithm | `docs/final/submission/performance_graphs/average_runtime.png` | Mean runtime for each algorithm across its recorded runs |
| Average memory per algorithm | `docs/final/submission/performance_graphs/average_memory.png` | Mean memory usage for each algorithm across its recorded runs |

## Average Measurements

| Algorithm | Average runtime (ns) | Average memory (KB) |
|---|---:|---:|
| A* Search | 624,744.83 | 239.43 |
| BFS Traversal | 39,124.71 | 263.61 |
| Bellman-Ford Shortest Path | 623,467.50 | 277.62 |
| Binary Search (Location Lookup) | 2,189.40 | 127.84 |
| DFS Traversal | 40,259.89 | 241.86 |
| Dijkstra Shortest Path | 657,127.62 | 342.11 |
| Kruskal MST (Road Network) | 416,103.00 | 233.67 |
| Linear Search (Resource Lookup) | 3,019.57 | 103.03 |
| Merge Sort (Urgency) | 575,799.20 | 615.68 |
| Priority Queue Dispatch | 1,206,949.00 | 796.51 |
| Quick Sort (Urgency) | 384,934.71 | 543.60 |
| Stack-based Undo | 7,094.71 | 183.39 |

## Observations

Binary search has the lowest average runtime in the provided performance dataset, followed by linear search and stack-based undo. BFS and DFS have similar average runtime values, which is reasonable for traversal work over the same network scale. Priority queue dispatch has the highest average runtime and memory use in the provided measurements. Sorting and shortest path algorithms show higher runtime and memory values than simple lookup algorithms.

These observations come only from the supplied `algorithm_Runs.csv` file. They should be described as measured dataset results, not universal algorithm benchmarks.
