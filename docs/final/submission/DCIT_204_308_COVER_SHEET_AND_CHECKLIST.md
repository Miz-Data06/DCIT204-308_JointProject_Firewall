# DCIT 204/308 Cover Sheet and Submission Checklist

## Cover Sheet

| Field | Value |
|---|---|
| Team name | Firewall |
| Selected Ghana context | Food delivery in Greater Accra, Ghana |
| Organisation/problem modelled | A food delivery service that manages locations, roads, riders, delivery requests, routing, request prioritisation, and dispatch decisions |
| Database used | SQLite through JDBC |
| Programming language/version | Java 17 |
| Total records in dataset | Locations: 150; Roads: 300; Delivery requests: 900; Riders/resources: 90; core operational records total: 1,440; algorithm_runs.csv: 60 filtered implemented-algorithm rows; audit_events.csv: 0 header-only rows; official CSV row total: 1,500 |
| Repository or submitted ZIP name | DCIT204-308_JointProject_Firewall |
| Repository URL | https://github.com/Miz-Data06/DCIT204-308_JointProject_Firewall.git |

## Student List

| Name | Student ID |
|---|---:|
| Augustine Yeboah Kumi | 22375677 |
| Emmanuel Eyram Korku Agbetor | 22206812 |
| Adu Asare Daniel | 22305775 |
| Hillary Aku Shika Allotey | 22047762 |
| Eyram Mami Araba Kumah | 22047897 |
| Nana yaw Marfo Agyei | 22397946 |
| Michael Akuffo Nyarko | 22411068 |
| Jedidiah Nii Saban Delali Annan | 22037871 |
| Sarpong Seth Appiah | 22372405 |
| Kingsbel Obese Sakyi | 22300123 |
| Musharafa Moro | 22059797 |
| Ampiah Samuel Abeka Sika | 22395143 |
| Wafaa Abdullah Layipana | 22383560 |
| Ishawu Abdul Manaf | 22406162 |

## Lecturer Checklist

| Requirement | Tick | Evidence location |
|---|---:|---|
| Local dataset with data dictionary | [x] | `data/`; `docs/final/submission/DATA_DICTIONARY.md`; `docs/final/DATABASE_AND_DATASET_DOCUMENTATION.md` |
| Database schema and seed data | [x] | `src/main/java/com/fooddelivery/database/`; `data/`; `docs/final/DATABASE_AND_DATASET_DOCUMENTATION.md` |
| Custom data structures implemented | [x] | `src/main/java/com/fooddelivery/datastructures/`; `docs/final/DATA_STRUCTURES_DOCUMENTATION.md` |
| Searching and sorting algorithms | [x] | `src/main/java/com/fooddelivery/algorithms/`; `docs/final/ALGORITHMS_DOCUMENTATION.md`; `evidence/search_sort_demo_output.txt` |
| Graph algorithms implemented | [x] | `src/main/java/com/fooddelivery/algorithms/`; `evidence/graph_traversal_demo_output.txt`; `evidence/dijkstra_demo_output.txt`; `evidence/mst_demo_output.txt` |
| Greedy and DP algorithms | [x] | `src/main/java/com/fooddelivery/algorithms/`; `evidence/optimization_demo_output.txt` |
| Correctness tests and trace tables | [x] | `src/test/java/com/fooddelivery/`; `docs/final/TRACE_TABLES_AND_DEMO_EVIDENCE.md`; `evidence/*.txt` |
| Performance CSV and graphs | [x] | `data/algorithm_runs.csv`; `docs/final/submission/PERFORMANCE_ANALYSIS.md`; `docs/final/submission/performance_graphs/` |
| Technical report | [x] | `docs/final/PROJECT_REPORT_DRAFT.md`; `docs/final/submission/Food_Delivery_System_Final_Report.docx`; `docs/final/submission/REPORT_CONVERSION_NOTES.md` |
| Demo video / oral defense prepared | [ ] | `docs/final/DEMO_SCRIPT.md`; `docs/final/submission/DEMO_SLIDES_OUTLINE.md`; To be prepared and presented by the team during final defense. |

## Repository Verification Snapshot

| Check | Result |
|---|---|
| Branch before artifact generation | `feature/application-integration` |
| Starting HEAD | `9134965 docs: add final project documentation package` |
| Final tests | `mvn clean test` passed with 513 tests |
| Final package | `mvn package` passed with 513 tests |
| Whitespace check | `git diff --check` passed |
| Generated DB/JAR/target files tracked | None found |
