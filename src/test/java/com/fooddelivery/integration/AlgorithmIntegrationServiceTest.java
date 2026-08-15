package com.fooddelivery.integration;

import com.fooddelivery.algorithms.result.MstResult;
import com.fooddelivery.algorithms.result.RequestSelectionResult;
import com.fooddelivery.algorithms.result.RouteResult;
import com.fooddelivery.algorithms.result.SearchResult;
import com.fooddelivery.algorithms.result.SortResult;
import com.fooddelivery.algorithms.result.TraversalResult;
import com.fooddelivery.database.mapper.DatasetLoadResult;
import com.fooddelivery.database.mapper.DatasetLoader;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.LocationType;
import com.fooddelivery.model.RequestStatus;
import com.fooddelivery.model.Rider;
import com.fooddelivery.model.Road;
import com.fooddelivery.model.VehicleType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlgorithmIntegrationServiceTest {
    @Test
    void buildsGraphFromSmallFixture() {
        DatasetLoadResult dataset = smallDataset();

        CustomGraph graph = new DeliveryNetworkBuilder().build(dataset);

        assertEquals(3, graph.vertexCount());
        assertEquals(2, graph.edgeCount());
        assertTrue(graph.containsEdge("LOC001", "LOC002"));
    }

    @Test
    void runsAlgorithmsOnSmallFixture() {
        AlgorithmDemoService service = new AlgorithmDemoService(smallDataset());

        SearchResult search = service.runSearchDemo("SR001");
        SortResult sort = service.runSortDemo();
        TraversalResult bfs = service.runBreadthFirstDemo("LOC001");
        TraversalResult dfs = service.runDepthFirstDemo("LOC001");
        RouteResult route = service.runFastestRouteDemo("LOC001", "LOC003");
        MstResult mst = service.runKruskalDemo();
        RequestSelectionResult selection = service.runRequestSelectionDemo(2, 1.0);

        assertTrue(search.isFound());
        assertEquals(2, sort.getSortedRequests().size());
        assertEquals(3, bfs.getOrder().size());
        assertEquals(3, dfs.getOrder().size());
        assertTrue(route.isReachable());
        assertEquals(2, route.getPath().size() - 1);
        assertEquals(2, mst.getSelectedRoads().size());
        assertEquals(1, selection.getSelectedRequests().size());
        assertTrue(service.runGreedyAssignmentDemo().isAssigned());
        assertTrue(service.runPriorityScoreDemo().getPriorityScore() > 0.0);
    }

    @Test
    void runsDatasetBackedSmokeDemos() {
        DatasetLoadResult dataset = new DatasetLoader().load(Path.of("data"));
        AlgorithmDemoService service = new AlgorithmDemoService(dataset);

        RouteResult route = service.runFastestRouteOnFirstRoad();
        MstResult kruskal = service.runKruskalDemo();
        RequestSelectionResult selection = service.runRequestSelectionDemo(5, 3.0);

        assertEquals(150, service.getGraph().vertexCount());
        assertEquals(300, service.getGraph().edgeCount());
        assertTrue(route.isReachable());
        assertTrue(kruskal.getVertexCount() == 150);
        assertTrue(kruskal.getSelectedRoads().size() > 0);
        assertEquals(5, selection.getInputCount());
        assertTrue(selection.getSelectedRequests().size() <= 3);
        assertNotNull(new RequestDispatchService(dataset, service.getGraph()).assignFirstRequest());
    }

    private static DatasetLoadResult smallDataset() {
        CustomDynamicArray<Location> locations = new CustomDynamicArray<>();
        locations.add(new Location("LOC001", "A", "Area", LocationType.RESTAURANT, 5.0, -0.1));
        locations.add(new Location("LOC002", "B", "Area", LocationType.CAMPUS, 5.1, -0.2));
        locations.add(new Location("LOC003", "C", "Area", LocationType.MARKET, 5.2, -0.3));

        CustomDynamicArray<Road> roads = new CustomDynamicArray<>();
        roads.add(new Road("ROAD001", "LOC001", "LOC002", 1.0, 5.0, 1.0));
        roads.add(new Road("ROAD002", "LOC002", "LOC003", 2.0, 10.0, 1.0));

        CustomDynamicArray<DeliveryRequest> requests = new CustomDynamicArray<>();
        LocalDateTime submitted = LocalDateTime.of(2026, 1, 1, 8, 0);
        requests.add(new DeliveryRequest("SR001", "LOC003", "LOC001", "Food Delivery", 3,
                1.0, submitted, submitted.plusHours(1), RequestStatus.PENDING, 0.9));
        requests.add(new DeliveryRequest("SR002", "LOC002", "LOC001", "Food Delivery", 1,
                1.0, submitted, submitted.plusHours(2), RequestStatus.PENDING, 0.2));

        CustomDynamicArray<Rider> riders = new CustomDynamicArray<>();
        riders.add(new Rider("RES001", "Rider RES001", "LOC001", VehicleType.MOTORCYCLE, 2.0, true));

        return new DatasetLoadResult(locations, roads, requests, riders);
    }
}
