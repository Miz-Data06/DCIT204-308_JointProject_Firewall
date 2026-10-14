# DCIT204-308_JointProject_FoodDelivery

A Java-based Ghana-localized service operations platform with custom data structures, graph algorithms, and JDBC database integration for the DCIT 204/308 joint semester project.

## Build and test

```powershell
mvn clean test
mvn package
git diff --check
```

## Run the GUI Demo

```powershell
mvn exec:java "-Dexec.mainClass=com.fooddelivery.gui.FoodDeliveryGuiApp"
```

The Swing GUI is the primary visual demo. It loads the project dataset, lets the user place a delivery request, computes the route and rider assignment result, and exposes the system summary data in the application.

## Console Fallback

```powershell
mvn exec:java "-Dexec.mainClass=com.fooddelivery.Main"
```

## Database and dataset setup

The project uses SQLite through JDBC and creates the schema at runtime. The operational data is loaded from the CSV files under `data/` and the schema is defined in `database/schema.sql` and the Java initializer under `src/main/java/com/fooddelivery/database`.

## Documentation

Final project documentation is available under [`docs/final/`](docs/final/). The current academic package includes:

- `docs/archive/PROJECT_REPORT_DRAFT.md` (historical archive)
- `docs/final/TEAM_CONTRIBUTIONS_AND_DEVELOPMENT_LOG.md`
- `docs/final/DSA_CORRECTNESS_EVIDENCE.md`
- `docs/final/AI_USE_ACKNOWLEDGEMENT.md`
- `docs/final/submission/PERFORMANCE_ANALYSIS.md`
- `docs/final/submission/performance/README.md`

## AI-use acknowledgement

This documentation package was prepared with AI-assisted drafting and evidence checking. All factual statements were checked against the repository history, documentation files, benchmark outputs, and test/build evidence before being retained. Claims that cannot be verified are marked as pending confirmation rather than stated as fact.
