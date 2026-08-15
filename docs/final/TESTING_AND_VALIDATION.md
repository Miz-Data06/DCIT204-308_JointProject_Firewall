# Testing and Validation

## Final Test Count

Final Maven test result:

- Tests run: `511`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

## Maven Commands Used

The final documentation phase verified the system with:

```powershell
mvn test
mvn package
git diff --check
```

Earlier final validation also used:

```powershell
mvn clean test
mvn package
git diff --check
```

## Package Build Result

`mvn package` completed successfully and built the project jar under `target/`. The jar is a generated build artifact and is not tracked in Git.

## Git Diff Check

`git diff --check` passed. This means there were no whitespace errors detected in the working changes at the check point.

## Console Smoke Test

The console workflow was run with scripted input and covered initialize/import dataset, counts, search/sort, BFS/DFS, Dijkstra, MST, request optimization, invalid input, and exit. The output is saved in `evidence/console_smoke_output.txt`.

## What Was Validated

| Area | Validation |
|---|---|
| Models | Constructor validation, equality, mutability rules, enum tests |
| Data structures | Unit tests for linear, queue, heap, hash, tree, graph, and disjoint-set structures |
| Algorithms | Unit tests for search, sort, BFS/DFS, Dijkstra, MST, request optimization, and priority scoring |
| CSV parsing | Quoted fields, invalid column counts, mapper behavior |
| Dataset mapping | Core row counts, enum transforms, generated road IDs, time normalization |
| Database | Schema creation, insert/read/count, duplicate replacement, foreign-key behavior, full CSV import counts |
| Integration | Graph build from dataset, route/traversal/MST/request selection smoke demos |
| Console | Exit, invalid input, dataset counts |

## Compliance Scans

The validation phase checked that generated `.db`, `.sqlite`, `.jar`, and `target/` files are not tracked; credentials were not found; legacy identity references were not found; SQLite JDBC dependency is present; repositories use prepared statements; and algorithm/data-structure internals were not changed during final validation.

## Known Non-Fatal Warning

SQLite JDBC prints an SLF4J no-operation logger warning during test/demo runs. This is not a test failure and does not affect database behavior. No extra logging dependency was added just to silence the warning.

## Remaining Non-Code Deliverables

- Convert `PROJECT_REPORT_DRAFT.md` into the required school submission format if a specific template is provided.
- Add lecturer/course cover-page details if required and not already covered.
- Prepare presentation slides or speaking notes from `DEMO_SCRIPT.md`.
- Confirm whether the project brief requires any extra appendix, contribution sheet, or signed declaration. This was **Not specified in provided brief/handoff** locally.
