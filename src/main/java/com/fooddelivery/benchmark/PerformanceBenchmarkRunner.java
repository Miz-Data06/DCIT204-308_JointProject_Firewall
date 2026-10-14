package com.fooddelivery.benchmark;

import com.fooddelivery.algorithms.BinarySearch;
import com.fooddelivery.algorithms.BreadthFirstSearch;
import com.fooddelivery.algorithms.DepthFirstSearch;
import com.fooddelivery.algorithms.DijkstraFastestRoute;
import com.fooddelivery.algorithms.KruskalMinimumSpanningTree;
import com.fooddelivery.algorithms.LinearSearch;
import com.fooddelivery.algorithms.PrimMinimumSpanningTree;
import com.fooddelivery.algorithms.RequestSortKey;
import com.fooddelivery.algorithms.SortBenchmarkOperations;
import com.fooddelivery.algorithms.result.SortResult;
import com.fooddelivery.datastructures.graphs.AdjacencyListGraph;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.hashing.CustomHashTable;
import com.fooddelivery.datastructures.heaps.PriorityHeap;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.datastructures.trees.CustomBST;
import com.fooddelivery.datastructures.trees.CustomRedBlackTree;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.LocationType;
import com.fooddelivery.model.RequestStatus;
import com.fooddelivery.model.Road;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

/** Reproducible measurements of the repository's actual implementations. */
public final class PerformanceBenchmarkRunner {
    private static final long SEED = 2043082026L;
    private static final int REPETITIONS = 3;
    private static final int WARMUPS = 1;
    private static final int[] REQUEST_SIZES = {100, 500, 1_000, 5_000, 10_000};
    private static final int[] LARGE_SIZES = {100, 1_000, 5_000, 10_000, 20_000};
    private static final int[] GRAPH_SIZES = {50, 100, 200, 500};

    private final ArrayList<Record> records = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        Path output = args.length == 0
                ? Path.of("docs", "final", "submission", "performance", "results")
                : Path.of(args[0]);
        new PerformanceBenchmarkRunner().run(output);
    }

    private void run(Path output) throws IOException {
        Files.createDirectories(output);
        benchmarkSearches();
        benchmarkSorts();
        benchmarkHashTables();
        benchmarkTrees();
        benchmarkHeaps();
        benchmarkGraphs();
        writeRaw(output.resolve("raw_timings.csv"));
        writeAggregate(output.resolve("aggregated_timings.csv"));
        writeEnvironment(output.resolve("environment.txt"));
        System.out.println("Wrote " + records.size() + " benchmark records to " + output.toAbsolutePath());
    }

    private void benchmarkSearches() {
        for (int size : REQUEST_SIZES) {
            CustomDynamicArray<DeliveryRequest> sorted = requests(size, true);
            String target = id(size - 1);
            measure("search", "linear_search", size, 0, 0, () -> new LinearSearch().search(sorted, target));
            measure("search", "binary_search", size, 0, 0, () -> new BinarySearch().search(sorted, target));
        }
    }

    private void benchmarkSorts() {
        for (int size : REQUEST_SIZES) {
            CustomDynamicArray<DeliveryRequest> input = shuffledRequests(size);
            for (String algorithm : new String[] {"selection_sort", "insertion_sort", "merge_sort", "quick_sort"}) {
                measure("sort", algorithm, size, 0, 0, () -> sortAndVerify(algorithm, input));
            }
        }
    }

    private void sortAndVerify(String algorithm, CustomDynamicArray<DeliveryRequest> input) {
        SortResult result = SortBenchmarkOperations.sortWithoutTrace(algorithm, input, RequestSortKey.REQUEST_ID);
        CustomDynamicArray<DeliveryRequest> sorted = result.getSortedRequests();
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i - 1).getRequestId().compareTo(sorted.get(i).getRequestId()) > 0) {
                throw new IllegalStateException(algorithm + " returned an unsorted result");
            }
        }
    }

    private void benchmarkHashTables() {
        for (int size : LARGE_SIZES) {
            for (int initialCapacity : new int[] {11, size * 2 + 1}) {
                for (int warmup = 0; warmup < WARMUPS; warmup++) hashWork(size, initialCapacity);
                for (int repetition = 1; repetition <= REPETITIONS; repetition++) {
                    long start = System.nanoTime();
                    HashOutcome outcome = hashWork(size, initialCapacity);
                    record("hash", "put_get", size, 0, repetition, System.nanoTime() - start,
                            outcome.loadFactor, outcome.collisions, "measured");
                }
            }
        }
    }

    private HashOutcome hashWork(int size, int initialCapacity) {
        CustomHashTable<CollidingKey, Integer> table = new CustomHashTable<>(initialCapacity);
        for (int i = 0; i < size; i++) table.put(new CollidingKey(i), i);
        for (int i = 0; i < size; i++) {
            if (table.get(new CollidingKey(i)) == null) throw new IllegalStateException("hash lookup failed");
        }
        return new HashOutcome(table.loadFactor(), size - 1L);
    }

    private void benchmarkTrees() {
        for (int size : LARGE_SIZES) {
            CustomBST<Integer, Integer> bst = new CustomBST<>();
            CustomRedBlackTree<Integer, Integer> redBlack = new CustomRedBlackTree<>();
            for (int i = 0; i < size; i++) { bst.insert(i, i); redBlack.insert(i, i); }
            for (int warmup = 0; warmup < WARMUPS; warmup++) { bst.search(size - 1); redBlack.search(size - 1); }
            for (int repetition = 1; repetition <= REPETITIONS; repetition++) {
                long start = System.nanoTime(); bst.search(size - 1);
                record("tree", "bst_search_increasing", size, 0, repetition, System.nanoTime() - start,
                        bst.height(), 0, "measured");
                start = System.nanoTime(); redBlack.search(size - 1);
                record("tree", "red_black_search_increasing", size, 0, repetition, System.nanoTime() - start,
                        redBlack.height(), 0, "measured");
            }
        }
    }

    private void benchmarkHeaps() {
        for (int size : LARGE_SIZES) {
            DeliveryRequest[] values = requestArray(size);
            measure("heap", "insert", size, 0, 0, () -> { PriorityHeap heap = new PriorityHeap(); for (DeliveryRequest value : values) heap.insert(value); });
            measure("heap", "extract", size, 0, 0, () -> { PriorityHeap heap = new PriorityHeap(); for (DeliveryRequest value : values) heap.insert(value); for (int i = 0; i < size; i++) heap.extractMax(); });
        }
    }

    private void benchmarkGraphs() {
        for (int size : GRAPH_SIZES) {
            CustomGraph graph = graph(size, size);
            String first = vertex(0), last = vertex(size - 1);
            measure("graph", "bfs", size, graph.edgeCount(), 0, () -> new BreadthFirstSearch().traverse(graph, first));
            measure("graph", "dfs", size, graph.edgeCount(), 0, () -> new DepthFirstSearch().traverse(graph, first));
            measure("graph", "dijkstra", size, graph.edgeCount(), 0, () -> new DijkstraFastestRoute().findRoute(graph, first, last));
            measure("graph", "prim", size, graph.edgeCount(), 0, () -> new PrimMinimumSpanningTree().build(graph, first));
            measure("graph", "kruskal", size, graph.edgeCount(), 0, () -> new KruskalMinimumSpanningTree().build(graph));
        }
    }

    private void measure(String family, String algorithm, int size, int edges, double metric, Runnable work) {
        for (int warmup = 0; warmup < WARMUPS; warmup++) work.run();
        for (int repetition = 1; repetition <= REPETITIONS; repetition++) {
            long start = System.nanoTime(); work.run();
            record(family, algorithm, size, edges, repetition, System.nanoTime() - start, metric, 0, "measured");
        }
    }

    private void limitation(String family, String algorithm, int size, String detail) {
        records.add(new Record(family, algorithm, size, 0, 0, 0, 0, 0, "limitation: " + detail));
    }

    private void record(String family, String algorithm, int size, int edgeCount, int repetition, long nanos, double metric, long secondary, String note) {
        records.add(new Record(family, algorithm, size, edgeCount, repetition, nanos, metric, secondary, note));
    }

    private void writeRaw(Path file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("family,algorithm,input_size,edge_count,repetition,elapsed_nanos,metric,secondary_metric,status\n");
            for (Record r : records) writer.write(r.csv() + "\n");
        }
    }

    private void writeAggregate(Path file) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("family,algorithm,input_size,edge_count,repetitions,mean_nanos,metric,secondary_metric,status\n");
            for (int i = 0; i < records.size(); i++) {
                Record first = records.get(i); if (first.repetition == 0) { writer.write(first.aggregate() + "\n"); continue; }
                if (first.repetition != 1) continue;
                long sum = 0; int count = 0;
                for (Record r : records) if (r.sameGroup(first) && r.repetition > 0) { sum += r.nanos; count++; }
                writer.write(first.aggregate(count, sum / count) + "\n");
            }
        }
    }

    private void writeEnvironment(Path file) throws IOException {
        String text = "seed=" + SEED + "\nrepetitions=" + REPETITIONS + "\nwarmup_runs=" + WARMUPS + "\n"
                + "java=" + System.getProperty("java.version") + "\nvm=" + System.getProperty("java.vm.name") + "\n"
                + "os=" + System.getProperty("os.name") + " " + System.getProperty("os.version") + "\n"
                + "cpu=" + System.getenv().getOrDefault("PROCESSOR_IDENTIFIER", "not reported by JVM") + "\n"
                + "jvm_args=" + ManagementFactory.getRuntimeMXBean().getInputArguments() + "\n";
        Files.writeString(file, text, StandardCharsets.UTF_8);
    }

    private static CustomDynamicArray<DeliveryRequest> requests(int size, boolean sorted) {
        CustomDynamicArray<DeliveryRequest> result = new CustomDynamicArray<>(size);
        DeliveryRequest[] values = requestArray(size);
        for (int i = 0; i < size; i++) result.add(values[sorted ? i : size - 1 - i]);
        return result;
    }

    private static CustomDynamicArray<DeliveryRequest> shuffledRequests(int size) {
        DeliveryRequest[] values = requestArray(size);
        Random random = new Random(SEED + size * 31L);
        for (int index = values.length - 1; index > 0; index--) {
            int other = random.nextInt(index + 1);
            DeliveryRequest temporary = values[index];
            values[index] = values[other];
            values[other] = temporary;
        }
        CustomDynamicArray<DeliveryRequest> result = new CustomDynamicArray<>(size);
        for (DeliveryRequest value : values) result.add(value);
        return result;
    }

    private static DeliveryRequest[] requestArray(int size) {
        DeliveryRequest[] values = new DeliveryRequest[size]; LocalDateTime now = LocalDateTime.of(2026, 1, 1, 8, 0);
        Random random = new Random(SEED + size);
        for (int i = 0; i < size; i++) {
            LocalDateTime submitted = now.plusSeconds(i);
            values[i] = new DeliveryRequest(id(i), "L0", "L1", "Benchmark", 1,
                    1.0, submitted, submitted.plusHours(1), RequestStatus.PENDING, random.nextDouble() * 100.0);
        }
        return values;
    }

    private static CustomGraph graph(int vertices, int edges) {
        CustomGraph graph = new AdjacencyListGraph();
        for (int i = 0; i < vertices; i++) graph.addVertex(new Location(vertex(i), "Location " + i, "Benchmark", LocationType.CAMPUS, 5.0 + i / 10000.0, -0.2));
        boolean[][] present = new boolean[vertices][vertices]; int added = 0;
        for (int i = 1; i < vertices; i++) { addRoad(graph, present, ++added, i - 1, i); }
        Random random = new Random(SEED + vertices);
        while (added < edges) { int a = random.nextInt(vertices), b = random.nextInt(vertices); if (a != b && !present[a][b]) addRoad(graph, present, ++added, a, b); }
        return graph;
    }

    private static void addRoad(CustomGraph graph, boolean[][] present, int number, int a, int b) {
        present[a][b] = true; present[b][a] = true;
        graph.addEdge(vertex(a), vertex(b), new Road("R" + number, vertex(a), vertex(b), 1.0, 1.0 + number % 10, 1.0));
    }

    private static String id(int index) { return String.format(Locale.ROOT, "REQ-%05d", index); }
    private static String vertex(int index) { return String.format(Locale.ROOT, "LOC-%04d", index); }

    private record HashOutcome(double loadFactor, long collisions) { }
    private record CollidingKey(int value) { @Override public int hashCode() { return 7; } }
    private record Record(String family, String algorithm, int size, int edgeCount, int repetition, long nanos, double metric, long secondary, String status) {
        String csv() { return family + ',' + algorithm + ',' + size + ',' + edgeCount + ',' + repetition + ',' + nanos + ',' + metric + ',' + secondary + ',' + status; }
        boolean sameGroup(Record other) { return family.equals(other.family) && algorithm.equals(other.algorithm) && size == other.size && edgeCount == other.edgeCount && Double.compare(metric, other.metric) == 0 && secondary == other.secondary && status.equals(other.status); }
        String aggregate() { return aggregate(0, 0); }
        String aggregate(int repetitions, long mean) { return family + ',' + algorithm + ',' + size + ',' + edgeCount + ',' + repetitions + ',' + mean + ',' + metric + ',' + secondary + ',' + status; }
    }
}
