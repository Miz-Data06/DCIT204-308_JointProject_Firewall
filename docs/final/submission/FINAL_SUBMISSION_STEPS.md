# Final Submission Steps

## Manual Review

- Confirm team name.
- Confirm repository or submitted ZIP name.
- Confirm all student names and IDs.
- Check lecturer formatting rules for the cover sheet, report, and slides.
- Review the DOCX report formatting and export it to PDF if required.
- Record or prepare the actual demo video/oral defense.

## Files to Submit

- Source repository or lecturer-required ZIP.
- `docs/final/submission/DCIT_204_308_COVER_SHEET_AND_CHECKLIST.md`
- `docs/final/submission/DATA_DICTIONARY.md`
- `docs/final/submission/PERFORMANCE_ANALYSIS.md`
- `docs/final/submission/performance_graphs/*.png`
- `docs/final/PROJECT_REPORT_DRAFT.md`
- `docs/final/submission/Food_Delivery_System_Final_Report.docx`
- Manually exported `Food_Delivery_System_Final_Report.pdf` if required.
- `docs/final/submission/DEMO_SLIDES_OUTLINE.md` or manually created slides.
- Evidence files under `evidence/`.

## How to Run the App

From the project root:

```powershell
mvn clean package
java -jar target/food-delivery-system-1.0-SNAPSHOT.jar
```

If running directly through Maven or an IDE, use `com.fooddelivery.Main`.

## Maven Verification Commands

```powershell
mvn test
mvn package
git diff --check
```

Expected result: 511 tests pass, package succeeds, and whitespace check succeeds.

## Demo Steps

1. Start the console application.
2. Load/import the dataset.
3. Show dataset counts.
4. Run search and sort demos.
5. Run BFS/DFS traversal demo.
6. Run Dijkstra fastest route demo.
7. Run Prim and Kruskal MST demo.
8. Run request optimisation and rider assignment demo.
9. Run priority scoring demo.
10. Exit the application cleanly.

## Git and Push Reminder

- Check `git status` before final submission.
- Do not include generated database files, generated jars, `target/`, temporary scripts, or cache folders.
- Push only when the group is ready and the lecturer submission rules allow it.

## ZIP Reminder

If the lecturer requires a ZIP, create it from the clean repository after final review. Do not include generated build folders unless the lecturer explicitly asks for them.
