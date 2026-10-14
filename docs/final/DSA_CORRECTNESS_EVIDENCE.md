# DSA correctness evidence

This document distinguishes executable evidence (the cited JUnit tests and public result traces) from the explanatory arguments below. All examples are deterministic fixtures in `src/test/java/com/fooddelivery/algorithms`; their locations use the project’s Ghana-localised `Legon` fixture data. No rows are reconstructed from production or benchmark data.

## Executable trace evidence

### 1. Binary search

Source and input: `SearchAlgorithmsTest.binarySearchFindsItemsOnSortedInputAndRecordsTrace`, using `AlgorithmTestFixtures`: `[REQ-001, REQ-002, REQ-003]`, key `REQ-002`. The source is already in ascending request-ID order, as required by `BinarySearch.search`.

| Step | low, high, middle | Middle ID and comparison with key | Decision / state |
|---|---|---|---|
| Initial | `0, 2, —` | sorted array | establish inclusive search interval |
| 1 | `0, 2, 1` | `REQ-002`, `compareTo = 0` | match; return index 1 and one-operation trace |
| Final | — | found `REQ-002` | `found=true`, `index=1` |

Verified by the result’s `BinarySearchStep` assertions in `SearchAlgorithmsTest`; implementation: `BinarySearch.java`.

### 2. Insertion sort

Source and input: `SortingAlgorithmsTest.sortsReturnDeterministicOrderAndTrace` with `[REQ-003, REQ-001, REQ-002]`, sorted by `REQUEST_ID`.

| Step | Current item / comparison | Decision | Working array after outer iteration |
|---|---|---|---|
| Initial | — | copied input; input is not mutated | `003,001,002` |
| `i=1` | `003 > 001` | shift `003`, insert `001` at 0 | `001,003,002` |
| `i=2` | `003 > 002`, then `001 <= 002` | shift `003`, stop, insert `002` at 1 | `001,002,003` |
| Final | all prefix positions processed | sorted copy returned | `REQ-001,REQ-002,REQ-003` |

The implementation emits one `SortTraceStep("insert", i, insertionIndex, snapshot)` after each displayed outer iteration. Verified by `SortingAlgorithmsTest`; implementation: `InsertionSort.java`.

### 3. Merge sort

Source and input: the same deterministic three-request fixture and key as trace 2, using `MergeSort.sort`.

| Step | Subarrays / comparison | Decision | Whole working array snapshot |
|---|---|---|---|
| Initial | indices `0..2` | split at 1, then split `0..1` at 0 | `003,001,002` |
| Merge `0..1` | compare `003` with `001` | take `001`, then copy remaining `003` | `001,003,002` |
| Merge `0..2` | `001 <= 002`; then `003 > 002` | take `001`, take `002`, copy remaining `003` | `001,002,003` |
| Final | merged interval `0..2` | return sorted copy | `REQ-001,REQ-002,REQ-003` |

`MergeSort` records a `merge` trace snapshot after each completed merge, so its trace contains the two merged states above. Verified by `SortingAlgorithmsTest`; implementation: `MergeSort.java`.

### 4. Dijkstra fastest route

Source and input: `DijkstraFastestRouteTest.fastestRouteUsesEffectiveTimeRatherThanPhysicalDistanceOnBothRepresentations`, `GraphAlgorithmFixtures.routeChoiceGraph`, source `A`, destination `D`. Roads are `A-B=8`, `B-D=8`, `A-C=3`, `C-D=3` effective minutes (the C route has 20 km physical distance but 6 minutes effective time).

| Step | Selected settled vertex | Relaxation / tentative time | Decision and distances |
|---|---|---|---|
| Initial | — | `d(A)=0`; all other distances infinity | predecessors empty |
| 1 | `A` | `A→B: 8`, `A→C: 3` | update `B←A`, `C←A`; `d(B)=8`, `d(C)=3` |
| 2 | `C` | `C→D: 6` | update `D←C`; `d(D)=6` |
| 3 | `D` | destination selected | stop; reconstruct `A,C,D` |
| Final | — | edge effective times `3,3` | reachable, total effective time 6, distance 20 |

The public `DijkstraStep` trace records the three unvisited-neighbour relaxations. Verified on both adjacency-list and adjacency-matrix graphs by the cited test; implementation: `DijkstraFastestRoute.java`.

### 5. Kruskal minimum spanning tree

Source and input: `MinimumSpanningTreeAlgorithmsTest.primAndKruskalProduceSameCostForConnectedGraphsOnBothRepresentations`, using its `weightedConnectedGraph`: `R-AB=1`, `R-AC=2`, `R-BD=3`, `R-CD=3`, `R-BC=5` effective cost.

| Sorted step | Road | Components before | Decision | Components after / cost |
|---|---|---|---|---|
| Initial | — | `{A},{B},{C},{D}` | initialise custom disjoint set | cost 0 |
| 1 | `R-AB (1)` | `{A},{B}` separate | accept union | `{A,B},{C},{D}`; cost 1 |
| 2 | `R-AC (2)` | `{A,B}`, `{C}` separate | accept union | `{A,B,C},{D}`; cost 3 |
| 3 | `R-BD (3)` | `{A,B,C}`, `{D}` separate | accept union; now `V-1` roads | `{A,B,C,D}`; cost 6 |
| Final | — | three selected roads | stop before later candidate roads | connected tree, cost 6 |

The implementation’s trace emits each examined road with the custom union result; a separate test verifies a rejected cycle. Verified by `MinimumSpanningTreeAlgorithmsTest`; implementation: `KruskalMinimumSpanningTree.java`.

### 6. Dynamic-programming request selection

Source and input: `RequestOptimisationAlgorithmsTest.dynamicProgrammingMatchesBruteForceOnSmallInputsAndReconstructs`: `A=(125,5)`, `B=(150,6)`, `C=(50,4)` where the first value is scaled capacity and the capacity limit is 175. This is the same fixture checked against brute force.

| DP row after item | Capacity ranges and best priority | Meaningful comparison / decision |
|---|---|---|
| 0 / none | `0..175: 0` | base case: no request gives zero priority |
| 1 / A | `0..124:0`; `125..175:5` | include A only where it fits |
| 2 / B | `0..124:0`; `125..149:5`; `150..175:6` | B improves every capacity where it fits, but cannot combine with A within 175 |
| 3 / C | `0..49:0`; `50..124:4`; `125..149:5`; `150..174:6`; `175:9` | at 175, `table[2][125]+4 = 9` beats B alone (6) |
| Reconstruction | begin at row 3, capacity 175 | take C, remaining 125; take A, remaining 0; return sorted `REQ-A,REQ-C` |

Final result is priority 9, scaled capacity 175, and eight brute-force states for comparison. The public result reports `dpRows=4`, `dpColumns=176`, and selected scaled capacity 175. Verified by `RequestOptimisationAlgorithmsTest`; implementation: `DynamicProgrammingKnapsack.java` and the shared comparison policy in `RequestSelectionSupport.java`.

## Correctness arguments

### Binary-search loop invariant

Assumptions: `BinarySearch` has first validated non-null, ascending request IDs and a non-blank key. Invariant: at the beginning of each loop iteration, if the key occurs, an occurrence is in the inclusive interval `[low, high]`. Initially the interval is the entire valid array. At each step, the middle comparison either finds the key, or sortedness proves every item through `middle` is too small (so raising `low` preserves the invariant), or every item from `middle` onward is too large (so lowering `high` preserves it). When the interval becomes empty, no possible occurrence remains; otherwise the equality branch returns a matching request. This is precisely the low/high update logic in `BinarySearch.java`.

### Merge-sort induction

Assumptions: `RequestComparators.compare` defines a consistent order for the chosen key and tie-breakers. Claim: `mergeSort(working,left,right,...)` returns that interval sorted and containing exactly its original requests. Base case `left >= right` has zero or one item and is sorted. For a larger interval, recursive calls sort the two halves by induction. `merge` compares their current least unmerged items, writes the lesser, and then copies the remaining already-sorted suffix. Thus the combined interval is ordered and preserves all items. The final call covers the copied input, so `MergeSort.sort` returns a sorted copy without input mutation.

### Dynamic-programming optimal-substructure argument

Assumptions: capacities are exactly scaled integers, priorities are finite non-negative decimal values, and each request is usable at most once. For row `i` and capacity `c`, the table entry represents the best selection from the first `i` requests under the shared order: greater exact priority, then lower used capacity, then lexicographically smaller selected IDs. Base row zero is the empty selection. For item `i`, every feasible selection either excludes it (the preceding-row entry) or includes it plus an optimal selection for `c - itemCapacity`; otherwise replacing that sub-selection would improve it. `isBetter` deterministically selects the better of those cases. Induction over rows therefore establishes optimality; the final scan applies the same order across capacities and reconstruction follows only chosen transitions.

## Counterexamples and boundary evidence

### Greedy rider assignment is not globally optimal

`GreedyCounterexampleScenario.demonstrate()` creates riders `R-A@A`, `R-B@B` and requests at `S1`, `S2`. Greedy assignment selects `R-A` for `REQ-1` because `A→S1=1`; assigning `R-B` to `REQ-2` costs 100, total 101. The global swap costs `B→S1=2` and `A→S2=2`, total 4. `RequestOptimisationAlgorithmsTest.greedyCounterexampleIsExecutableAndShowsBetterGlobalAssignment` verifies the executable result. This demonstrates the known limitation of independent local assignment, not a defect in the single-request greedy contract.

### Unsorted binary-search input violates a precondition

`SearchAlgorithmsTest.binarySearchRejectsUnsortedInputCounterexample` passes `[REQ-002, REQ-001]` while searching for `REQ-001`. The ordering validation detects `REQ-002 > REQ-001` and throws `IllegalArgumentException` before any binary-search step. This guards the loop invariant above; it is invalid input, not a binary-search defect.

## Scope and limits

These traces are intentionally small enough to audit. They validate public result traces and deterministic JUnit fixtures, not every internal array assignment. Edge-case tests also cover empty search inputs, missing routes, source equal to destination, disconnected forests, zero capacity, invalid scaled capacity, ties, and DP safety limits. The traces were verified against the current implementation and cited tests; none could be verified from a committed demonstration-output artifact because the project stores these traces as test/result objects rather than checked-in console transcripts.
