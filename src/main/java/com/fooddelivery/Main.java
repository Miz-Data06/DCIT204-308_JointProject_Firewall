package com.fooddelivery;

import com.fooddelivery.algorithms.result.MstResult;
import com.fooddelivery.algorithms.result.PriorityScoreResult;
import com.fooddelivery.algorithms.result.RequestSelectionResult;
import com.fooddelivery.algorithms.result.RiderAssignmentResult;
import com.fooddelivery.algorithms.result.RouteResult;
import com.fooddelivery.algorithms.result.SearchResult;
import com.fooddelivery.algorithms.result.SortResult;
import com.fooddelivery.algorithms.result.TraversalResult;
import com.fooddelivery.database.DatabaseConfig;
import com.fooddelivery.database.DatabaseConnectionFactory;
import com.fooddelivery.database.DatabaseInitializer;
import com.fooddelivery.database.DatasetDatabaseImporter;
import com.fooddelivery.database.mapper.DatasetLoadResult;
import com.fooddelivery.database.mapper.DatasetLoader;
import com.fooddelivery.integration.AlgorithmDemoService;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.Road;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        run(new Scanner(System.in), System.out);
    }

    static void run(Scanner scanner, PrintStream output) {
        ConsoleWorkflow workflow = new ConsoleWorkflow(output);
        output.println("Food Delivery System");
        boolean running = true;
        while (running) {
            printMenu(output);
            String choice = scanner.hasNextLine() ? scanner.nextLine().trim() : "8";
            try {
                running = workflow.handle(choice);
            } catch (RuntimeException exception) {
                output.println("Error: " + exception.getMessage());
            }
        }
    }

    private static void printMenu(PrintStream output) {
        output.println();
        output.println("1. Initialize/import dataset");
        output.println("2. Show counts");
        output.println("3. Run search/sort demo");
        output.println("4. Run BFS/DFS traversal demo");
        output.println("5. Run Dijkstra fastest route demo");
        output.println("6. Run MST demo");
        output.println("7. Run request optimization and priority demo");
        output.println("8. Exit");
        output.print("Select option: ");
    }

    private static final class ConsoleWorkflow {
        private final PrintStream output;
        private DatasetLoadResult dataset;
        private AlgorithmDemoService demoService;
        private boolean imported;

        private ConsoleWorkflow(PrintStream output) {
            this.output = output;
        }

        private boolean handle(String choice) {
            return switch (choice) {
                case "1" -> {
                    initialize();
                    yield true;
                }
                case "2" -> {
                    showCounts();
                    yield true;
                }
                case "3" -> {
                    runSearchSort();
                    yield true;
                }
                case "4" -> {
                    runTraversal();
                    yield true;
                }
                case "5" -> {
                    runDijkstra();
                    yield true;
                }
                case "6" -> {
                    runMst();
                    yield true;
                }
                case "7" -> {
                    runOptimization();
                    yield true;
                }
                case "8" -> {
                    output.println("Goodbye.");
                    yield false;
                }
                default -> {
                    output.println("Invalid option. Choose 1-8.");
                    yield true;
                }
            };
        }

        private void initialize() {
            ensureLoaded();
            DatabaseConnectionFactory factory = new DatabaseConnectionFactory(DatabaseConfig.defaultConfig());
            new DatabaseInitializer(factory).initialize();
            new DatasetDatabaseImporter(factory).importDataset(dataset);
            imported = true;
            output.println("Dataset loaded and imported to target/food_delivery.db.");
            printCounts();
        }

        private void showCounts() {
            ensureLoaded();
            printCounts();
            output.println(imported ? "Database import: complete" : "Database import: not run in this session");
        }

        private void runSearchSort() {
            AlgorithmDemoService service = ensureService();
            SearchResult search = service.runSearchDemo("SR001");
            SortResult sort = service.runSortDemo();
            output.println("Search SR001 found=" + search.isFound() + ", operations=" + search.getOperationCount());
            if (!sort.getSortedRequests().isEmpty()) {
                DeliveryRequest top = sort.getSortedRequests().get(0);
                output.println("Top sorted request=" + top.getRequestId() + ", priority=" + top.getPriorityScore());
            }
        }

        private void runTraversal() {
            AlgorithmDemoService service = ensureService();
            Location start = dataset.getLocations().get(0);
            TraversalResult bfs = service.runBreadthFirstDemo(start.getLocationId());
            TraversalResult dfs = service.runDepthFirstDemo(start.getLocationId());
            output.println("BFS visited=" + bfs.getOrder().size() + ", start=" + start.getLocationId());
            output.println("DFS visited=" + dfs.getOrder().size() + ", start=" + start.getLocationId());
        }

        private void runDijkstra() {
            AlgorithmDemoService service = ensureService();
            Road road = dataset.getRoads().get(0);
            RouteResult route = service.runFastestRouteDemo(road.getFromLocationId(), road.getToLocationId());
            output.println("Dijkstra " + road.getFromLocationId() + " -> " + road.getToLocationId()
                    + ": reachable=" + route.isReachable()
                    + ", effectiveTime=" + route.getTotalEffectiveTime()
                    + ", pathNodes=" + route.getPath().size());
        }

        private void runMst() {
            AlgorithmDemoService service = ensureService();
            Location start = dataset.getLocations().get(0);
            MstResult prim = service.runPrimDemo(start.getLocationId());
            MstResult kruskal = service.runKruskalDemo();
            output.println("Prim selectedRoads=" + prim.getSelectedRoads().size()
                    + ", components=" + prim.getComponentCount());
            output.println("Kruskal selectedRoads=" + kruskal.getSelectedRoads().size()
                    + ", components=" + kruskal.getComponentCount());
        }

        private void runOptimization() {
            AlgorithmDemoService service = ensureService();
            RequestSelectionResult selection = service.runRequestSelectionDemo(10, 5.0);
            RiderAssignmentResult assignment = service.runGreedyAssignmentDemo();
            PriorityScoreResult priority = service.runPriorityScoreDemo();
            output.println("DP selectedRequests=" + selection.getSelectedRequests().size()
                    + ", totalPriority=" + selection.getTotalPriority());
            output.println("Greedy rider assigned=" + assignment.isAssigned()
                    + (assignment.isAssigned() ? ", rider=" + assignment.getSelectedRider().getRiderId() : ""));
            output.println("Priority demo score=" + priority.getPriorityScore());
        }

        private AlgorithmDemoService ensureService() {
            ensureLoaded();
            if (demoService == null) {
                demoService = new AlgorithmDemoService(dataset);
            }
            return demoService;
        }

        private void ensureLoaded() {
            if (dataset == null) {
                dataset = new DatasetLoader().load(Path.of("data"));
            }
        }

        private void printCounts() {
            output.println("Locations=" + dataset.getLocations().size()
                    + ", roads=" + dataset.getRoads().size()
                    + ", requests=" + dataset.getDeliveryRequests().size()
                    + ", riders=" + dataset.getRiders().size());
        }
    }
}
