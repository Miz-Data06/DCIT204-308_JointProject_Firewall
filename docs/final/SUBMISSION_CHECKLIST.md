# Submission Checklist

## Code

- [x] Java package uses `com.fooddelivery`.
- [x] Models are implemented.
- [x] Custom data structures are implemented.
- [x] Algorithms are implemented.
- [x] CSV/database integration is implemented.
- [x] Console workflow is implemented.
- [x] Production Java code was not changed during documentation phase.

## Tests

- [x] `mvn test` passes.
- [x] Final test count is 511.
- [x] There are 0 failures, 0 errors, and 0 skipped tests.
- [x] `mvn package` passes.
- [x] `git diff --check` passes.

## Dataset

- [x] `Locations.csv` present.
- [x] `Roads_Edges.csv` present.
- [x] `Service Request.csv` present.
- [x] `Resource.csv` present.
- [x] `algorithm_Runs.csv` present.
- [x] `audit_Events.csv` present.
- [x] Core import counts verified: 150 locations, 300 roads, 900 requests, 90 riders/resources.

## Database

- [x] SQLite JDBC dependency present.
- [x] Runtime DB path is `target/food_delivery.db`.
- [x] Generated database files are not tracked.
- [x] Schema creates locations, roads, riders, and delivery requests.
- [x] Repositories use prepared statements.
- [x] CSV-to-database import counts tested.

## Evidence

- [x] Console smoke output saved.
- [x] Dataset counts saved.
- [x] Search/sort evidence saved.
- [x] BFS/DFS evidence saved.
- [x] Dijkstra evidence saved.
- [x] MST evidence saved.
- [x] Optimization evidence saved.
- [x] Priority scoring evidence saved.

## Report

- [x] Draft report created at `docs/final/PROJECT_REPORT_DRAFT.md`.
- [x] Data structures documentation created.
- [x] Algorithms documentation created.
- [x] Database/dataset documentation created.
- [x] Testing/validation documentation created.
- [x] Trace/evidence summary created.
- [ ] Convert draft report to required school format if a separate template is provided.
- [ ] Add any lecturer-specific cover sheet details if required.

## Demo

- [x] Demo script created at `docs/final/DEMO_SCRIPT.md`.
- [x] Menu options and expected highlights documented.
- [x] Dijkstra, MST, database import, and optimization explanations included.
- [ ] Practice the demo using the final branch.
- [ ] Prepare slides if required.

## Student Details

- [x] Student table included in documentation files only.
- [x] Student names/IDs were not added to production source code.
- [ ] Confirm spelling/capitalization of all names before final submission.

## Git Status

- [ ] Run `git status --short` before final submission.
- [ ] Confirm no generated `.db`, `.sqlite`, `.jar`, or `target/` files are tracked.
- [ ] Confirm documentation commit is present.
- [ ] Push only when the team is ready and the submission process requires it.

## Push / Submission Steps

- [ ] Review final documentation in `docs/final/`.
- [ ] Run `mvn test`.
- [ ] Run `mvn package`.
- [ ] Run `git diff --check`.
- [ ] Commit any final approved documentation-only changes.
- [ ] Push the final branch only when requested.
- [ ] Submit repository link, report, evidence, and any school-required files.
