# Reproducible DSA performance experiments

Run from the repository root after compiling:

```powershell
mvn -q -DskipTests compile
mvn -q exec:java "-Dexec.mainClass=com.fooddelivery.benchmark.PerformanceBenchmarkRunner"
python docs/final/submission/performance/plot_results.py
```

The runner uses seed `2043082026`, one untimed warm-up, and three timed repetitions. It writes raw repetitions, arithmetic means, and the JVM/OS/CPU metadata to `results/`. Timed boundaries surround only the algorithm operation: request and graph construction occur before timing. Sorting uses its package-internal trace-disabled path, while the public methods retain their trace-producing behaviour.

`raw_timings.csv` has one row per timed repetition. `aggregated_timings.csv` has the mean for every measured group. The `metric` column is load factor for hash tables and height for trees; `secondary_metric` is collision count for hash tables. For graphs, `edge_count` is explicit. Collision tests deliberately use distinct keys with one shared hash code, so the reported collision count is exactly `n - 1` and exercises separate chaining. The hash table has a fixed internal 0.75 resize threshold; varying initial capacity produces observed final load factors rather than changing its implementation.

The ordinary BST is deliberately populated in increasing-key order, exposing its skewed height and search behaviour. The red-black tree uses the same keys. Search, heap, graph, and all four sorting experiments include every required size. The public sort APIs still create full trace snapshots for instructional evidence. The benchmark path executes the same comparator, copy, comparison, swap, merge, and partition code while omitting only `SortTraceStep` construction, so it measures algorithm-only cost without changing public behaviour.

Expected theory is linear versus logarithmic search; selection/insertion quadratic versus merge/quick average `O(n log n)`; hash-table operations average `O(1)` but chain linearly under forced collisions; increasing inserts make the BST height `n - 1` while the red-black tree remains logarithmic; heap insertion/extraction are `O(log n)` each; BFS/DFS are `O(V+E)` subject to neighbour sorting, Dijkstra uses a vertex scan, Prim scans frontier roads, and Kruskal sorts roads.

Plots use Python 3 with `matplotlib` (install with `python -m pip install matplotlib`). They are derived only from `results/aggregated_timings.csv`. The optional heap-memory value sometimes reported by JVM tools is not collected: it includes unrelated live objects, garbage collection timing, JIT state, and allocator effects, so it cannot be attributed reliably to one operation without a dedicated profiler.

`data/algorithm_runs.csv` is preserved historical/imported data with unverified provenance. It is not read, overwritten, combined with, or presented as current benchmark output.
