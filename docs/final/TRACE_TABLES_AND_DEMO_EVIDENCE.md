# Trace Tables and Demo Evidence

Evidence files are stored in `evidence/`. This document summarizes the useful demo points without pasting all raw output.

## Dataset Counts

| Item | Count |
|---|---:|
| Locations | 150 |
| Roads | 300 |
| Delivery requests | 900 |
| Riders/resources | 90 |
| Graph vertices | 150 |
| Graph edges | 300 |

## Search and Sort Evidence

- Linear search for `SR001`: found at index `0` with `1` operation.
- Binary search for `SR450`: found at index `449` with `1` operation.
- Binary trace sample: `low=0`, `high=899`, `mid=449`, `middleRequestId=SR450`, `comparison=0`.
- Quicksort on the first 20 requests by priority descending used `69` operations.
- Top sorted requests: `SR018`, `SR015`, `SR012`, `SR006`, `SR003`.

## BFS and DFS Evidence

- BFS from `LOC001` visited `150` locations.
- DFS from `LOC001` visited `150` locations.
- BFS sample begins: `LOC001 -> LOC074 -> LOC090 -> LOC119 -> LOC005`.
- DFS sample begins: `LOC001 -> LOC074 -> LOC090 -> LOC005 -> LOC025`.

This shows the dataset road graph is connected from the selected start location.

## Dijkstra Evidence

- Source: `LOC073`
- Destination: `LOC076`
- Reachable: `true`
- Path: `LOC073 -> LOC076`
- Total distance: `1.68 km`
- Total effective travel time: `6.80`
- Relaxation steps recorded: `8`

Example relaxation:

`current=LOC073, neighbour=LOC076, tentative=6.80, updated=true`

## MST Evidence

| Algorithm | Total effective cost | Selected roads | Components |
|---|---:|---:|---:|
| Prim | 1177.18 | 149 | 1 |
| Kruskal | 1177.18 | 149 | 1 |

Prim selected edge sample includes `ROAD130: LOC001 <-> LOC074, effective=2.40`. Kruskal selected edge sample includes `ROAD168: LOC049 <-> LOC019, effective=1.20`. Both algorithms produce the same total cost, which is useful evidence that the MST implementation is consistent.

## Optimization Evidence

- Brute force on first 5 requests with capacity `3.0`: selected `SR002`, `SR003`, `SR005`; total priority `1.7857`; states considered `32`.
- Dynamic programming on first 10 requests with capacity `5.0`: selected `SR003`, `SR005`, `SR006`, `SR008`, `SR009`; total priority `3.2606`; states considered `5511`.
- Greedy assignment on the first dataset request: assigned `true`, rider `RES006`, pickup time `38.80`.
- Greedy counterexample: greedy total `6.0`, better total `4.0`, demonstrates failure `true`.

## Priority Score Evidence

Approved weights:

- urgency: `58 / 204 = 0.28431372549019607`
- deadline: `74 / 204 = 0.3627450980392157`
- waiting: `72 / 204 = 0.35294117647058826`

Sample inputs are urgency `1.0`, deadline `0.75`, and waiting `0.5`. The sample score is `0.732843137254902`.

## Console Smoke Summary

The menu successfully ran dataset import, counts, search/sort, BFS/DFS, Dijkstra, MST, optimization and priority scoring, invalid input handling, and clean exit.

Useful output highlights:

- `Dataset loaded and imported to target/food_delivery.db.`
- `Locations=150, roads=300, requests=900, riders=90`
- `Search SR001 found=true, operations=1`
- `BFS visited=150, start=LOC001`
- `Dijkstra LOC073 -> LOC076: reachable=true, effectiveTime=6.8, pathNodes=2`
- `Prim selectedRoads=149, components=1`
- `DP selectedRequests=5, totalPriority=3.2606209150326793`
- `Invalid option. Choose 1-8.`
- `Goodbye.`
