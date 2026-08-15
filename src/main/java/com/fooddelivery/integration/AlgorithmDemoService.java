package com.fooddelivery.integration;

import com.fooddelivery.algorithms.BreadthFirstSearch;
import com.fooddelivery.algorithms.DepthFirstSearch;
import com.fooddelivery.algorithms.DijkstraFastestRoute;
import com.fooddelivery.algorithms.DynamicProgrammingKnapsack;
import com.fooddelivery.algorithms.GreedyRiderAssignment;
import com.fooddelivery.algorithms.KruskalMinimumSpanningTree;
import com.fooddelivery.algorithms.LinearSearch;
import com.fooddelivery.algorithms.PrimMinimumSpanningTree;
import com.fooddelivery.algorithms.PriorityScoreCalculator;
import com.fooddelivery.algorithms.QuickSort;
import com.fooddelivery.algorithms.RequestSortKey;
import com.fooddelivery.algorithms.result.MstResult;
import com.fooddelivery.algorithms.result.PriorityScoreResult;
import com.fooddelivery.algorithms.result.RequestSelectionResult;
import com.fooddelivery.algorithms.result.RiderAssignmentResult;
import com.fooddelivery.algorithms.result.RouteResult;
import com.fooddelivery.algorithms.result.SearchResult;
import com.fooddelivery.algorithms.result.SortResult;
import com.fooddelivery.algorithms.result.TraversalResult;
import com.fooddelivery.database.mapper.DatasetLoadResult;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Road;

public class AlgorithmDemoService {
    private final DatasetLoadResult dataset;
    private final CustomGraph graph;

    public AlgorithmDemoService(DatasetLoadResult dataset) {
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset must not be null");
        }
        this.dataset = dataset;
        this.graph = new DeliveryNetworkBuilder().build(dataset);
    }

    public CustomGraph getGraph() {
        return graph;
    }

    public SearchResult runSearchDemo(String requestId) {
        return new LinearSearch().search(dataset.getDeliveryRequests(), requestId);
    }

    public SortResult runSortDemo() {
        return new QuickSort().sort(firstRequests(20), RequestSortKey.PRIORITY_SCORE, true, null);
    }

    public TraversalResult runBreadthFirstDemo(String startLocationId) {
        return new BreadthFirstSearch().traverse(graph, startLocationId);
    }

    public TraversalResult runDepthFirstDemo(String startLocationId) {
        return new DepthFirstSearch().traverse(graph, startLocationId);
    }

    public RouteResult runFastestRouteDemo(String sourceLocationId, String destinationLocationId) {
        return new DijkstraFastestRoute().findRoute(graph, sourceLocationId, destinationLocationId);
    }

    public RouteResult runFastestRouteOnFirstRoad() {
        Road road = dataset.getRoads().get(0);
        return runFastestRouteDemo(road.getFromLocationId(), road.getToLocationId());
    }

    public MstResult runPrimDemo(String startLocationId) {
        return new PrimMinimumSpanningTree().build(graph, startLocationId);
    }

    public MstResult runKruskalDemo() {
        return new KruskalMinimumSpanningTree().build(graph);
    }

    public RequestSelectionResult runRequestSelectionDemo(int requestLimit, double riderCapacity) {
        return new DynamicProgrammingKnapsack().select(firstRequests(requestLimit), riderCapacity);
    }

    public RiderAssignmentResult runGreedyAssignmentDemo() {
        return new GreedyRiderAssignment().assign(graph, dataset.getRiders(), dataset.getDeliveryRequests().get(0));
    }

    public PriorityScoreResult runPriorityScoreDemo() {
        return new PriorityScoreCalculator().calculate(1.0, 0.75, 0.5);
    }

    public DatasetLoadResult getDataset() {
        return dataset;
    }

    private CustomDynamicArray<DeliveryRequest> firstRequests(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Request limit must be positive");
        }
        int count = Math.min(limit, dataset.getDeliveryRequests().size());
        CustomDynamicArray<DeliveryRequest> requests = new CustomDynamicArray<>(count == 0 ? 1 : count);
        for (int i = 0; i < count; i++) {
            requests.add(dataset.getDeliveryRequests().get(i));
        }
        return requests;
    }
}
