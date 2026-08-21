# Final Submission Steps

## Manual Review

- Confirm all student names and IDs.
- Check lecturer formatting rules for the cover sheet, report, and slides.
- Review the DOCX report formatting and export it to PDF if required.
- To be prepared and presented by the team during final defense.

## Files to Submit

- Source repository: `https://github.com/Miz-Data06/DCIT204-308_JointProject_Firewall.git`
- Repository or ZIP name: `DCIT204-308_JointProject_Firewall`
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

Expected result: 513 tests pass, package succeeds, and whitespace check succeeds.

## Demo Steps

1. Start the GUI/application experience.
2. Place or request a delivery.
3. Show the system handling the request.
4. View the route or result output.
5. Assign a rider/resource.
6. Show dataset-backed information such as locations, roads, requests, or riders.
7. Explain at a high level that custom data structures and algorithms support the visible workflow.
8. Use the console workflow only as a technical fallback if the GUI is unavailable.

## Git and Push Reminder

- Check `git status` before final submission.
- Do not include generated database files, generated jars, `target/`, temporary scripts, or cache folders.
- Push the completed `feature/application-integration` branch to `origin` when the group is ready and the lecturer submission rules allow it.

## ZIP Reminder

If the lecturer requires a ZIP, create it from the clean repository after final review. Do not include generated build folders unless the lecturer explicitly asks for them.
