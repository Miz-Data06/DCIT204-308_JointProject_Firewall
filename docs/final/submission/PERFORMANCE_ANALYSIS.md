# Performance Analysis

This report uses the reproducible timing outputs generated under `docs/final/submission/performance/results/`. The current benchmark package records both the raw repetitions (`raw_timings.csv`) and the aggregated means (`aggregated_timings.csv`), and the generated charts are stored as SVG files in the same directory.

The historical `data/algorithm_runs.csv` file remains a preserved dataset artifact and is not treated as the current benchmark output. The performance narrative therefore refers to the tracked reproduction under `docs/final/submission/performance/results/` unless a separate historical import is explicitly being discussed.

## Generated Graphs

| Graph | File | What it shows |
|---|---|---|
| Search timings | `docs/final/submission/performance/results/search_timings.svg` | Measured search-family runtime trend across the tested input sizes |
| Sort timings | `docs/final/submission/performance/results/sort_timings.svg` | Measured sort-family runtime trend across the tested input sizes |
| Graph timings | `docs/final/submission/performance/results/graph_timings.svg` | Measured graph-family runtime trend across the tested graph sizes |
| Heap timings | `docs/final/submission/performance/results/heap_timings.svg` | Measured heap-family runtime trend across the tested sizes |
| Hash timings | `docs/final/submission/performance/results/hash_timings.svg` | Measured hash-family runtime and load-factor behaviour |
| Tree timings | `docs/final/submission/performance/results/tree_timings.svg` | Measured tree-family runtime and height behaviour |

## Measurement Method

The benchmark package uses a fixed seed, one untimed warm-up, and three timed repetitions for each measured algorithm family. The repository records the runtime means in `aggregated_timings.csv`, while the raw per-run timings remain in `raw_timings.csv`. The `metric` and `secondary_metric` columns are preserved for the algorithm-specific structural measurements such as load factor, height, and collision count; those values are interpreted in the benchmark README rather than treated as a universal benchmark claim.

## Observations

The benchmark output demonstrates the expected qualitative relationships for the implemented algorithms: search and traversal routines scale differently from sorting and graph algorithms, the hash table shows average constant-time behaviour under normal insertion but chain growth under deliberate collisions, and the tree experiments distinguish the skewed ordinary BST from the balanced red-black tree. These are measured repository observations, not general claims about every possible dataset or runtime environment.

The benchmark evidence is intentionally scoped to the project’s reproducible local execution and should be read as coursework-appropriate performance analysis rather than a universal algorithm benchmark.
