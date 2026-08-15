# Performance Analysis

The performance checklist uses the official file `data/algorithm_runs.csv`. The CSV contains 60 measured rows filtered to algorithms implemented in this project, with these fields: run id, algorithm name, input size, run number, runtime in nanoseconds, memory in kilobytes, and execution date/time.

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
| BFS Traversal | 39,124.71 | 263.61 |
| Binary Search (Location Lookup) | 2,189.40 | 127.84 |
| DFS Traversal | 40,259.89 | 241.86 |
| Dijkstra Shortest Path | 657,127.62 | 342.11 |
| Kruskal MST (Road Network) | 416,103.00 | 233.67 |
| Linear Search (Resource Lookup) | 3,019.57 | 103.03 |
| Merge Sort (Urgency) | 575,799.20 | 615.68 |
| Quick Sort (Urgency) | 384,934.71 | 543.60 |

## Observations

Binary search has the lowest average runtime in the filtered performance dataset, followed by linear search. BFS and DFS have similar average runtime values, which is reasonable for traversal work over the same network scale. Sorting and shortest path algorithms show higher runtime and memory values than simple lookup algorithms.

These observations come only from the supplied `algorithm_runs.csv` file. They should be described as measured dataset results, not universal algorithm benchmarks.
