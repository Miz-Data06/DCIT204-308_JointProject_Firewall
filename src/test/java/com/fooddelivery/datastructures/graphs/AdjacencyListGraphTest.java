package com.fooddelivery.datastructures.graphs;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.LocationType;
import com.fooddelivery.model.Road;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdjacencyListGraphTest {
    @Test
    void newGraphStartsEmptyAndClearIsSafe() {
        AdjacencyListGraph graph = new AdjacencyListGraph();

        assertEquals(0, graph.vertexCount());
        assertEquals(0, graph.edgeCount());
        assertEquals(0, graph.getVertices().size());
        assertEquals(0, graph.getEdges().size());

        graph.clear();

        assertEquals(0, graph.vertexCount());
        assertEquals(0, graph.edgeCount());
        assertEquals(0, graph.getVertices().size());
    }

    @Test
    void addVertexStoresAndUpdatesByLocationIdWithoutRemovingEdges() {
        AdjacencyListGraph graph = new AdjacencyListGraph();
        Location first = location("LOC-B", "Bistro", LocationType.RESTAURANT);
        Location second = location("LOC-A", "Apartments", LocationType.RESIDENTIAL_AREA);
        Location replacement = location("LOC-B", "Better Bistro", LocationType.RESTAURANT);
        graph.addVertex(first);
        graph.addVertex(second);
        graph.addEdge("LOC-A", "LOC-B", road("R-1", "LOC-A", "LOC-B", 4.0, 10.0, 1.5));

        graph.addVertex(replacement);

        assertEquals(2, graph.vertexCount());
        assertSame(replacement, graph.getVertex("LOC-B"));
        assertTrue(graph.containsVertex("LOC-B"));
        assertTrue(graph.containsEdge("LOC-A", "LOC-B"));
        CustomDynamicArray<Location> vertices = graph.getVertices();
        assertSame(second, vertices.get(0));
        assertSame(replacement, vertices.get(1));
    }

    @Test
    void vertexValidationRejectsNullBlankAndMissingRequiredVertices() {
        AdjacencyListGraph graph = new AdjacencyListGraph();

        assertThrows(IllegalArgumentException.class, () -> graph.addVertex(null));
        assertThrows(IllegalArgumentException.class, () -> graph.containsVertex(null));
        assertThrows(IllegalArgumentException.class, () -> graph.getVertex(" "));
        assertThrows(IllegalArgumentException.class, () -> graph.getNeighbours("LOC-X"));
        assertThrows(IllegalArgumentException.class, () -> graph.removeVertex("LOC-X"));
        assertFalse(graph.containsVertex("LOC-X"));
        assertNull(graph.getVertex("LOC-X"));
    }

    @Test
    void vertexSnapshotsAreSortedAndIndependent() {
        AdjacencyListGraph graph = graphWithVertices("LOC-C", "LOC-A", "LOC-B");

        CustomDynamicArray<Location> snapshot = graph.getVertices();
        snapshot.remove(0);
        CustomDynamicArray<Location> laterSnapshot = graph.getVertices();

        assertEquals(2, snapshot.size());
        assertEquals(3, laterSnapshot.size());
        assertEquals("LOC-A", laterSnapshot.get(0).getLocationId());
        assertEquals("LOC-B", laterSnapshot.get(1).getLocationId());
        assertEquals("LOC-C", laterSnapshot.get(2).getLocationId());
    }

    @Test
    void addEdgeCreatesBothDirectionsAndEffectiveWeight() {
        AdjacencyListGraph graph = graphWithVertices("LOC-A", "LOC-B");
        Road road = road("R-1", "LOC-A", "LOC-B", 3.5, 8.0, 1.25);

        graph.addEdge("LOC-A", "LOC-B", road);

        assertEquals(1, graph.edgeCount());
        assertTrue(graph.containsEdge("LOC-A", "LOC-B"));
        assertTrue(graph.containsEdge("LOC-B", "LOC-A"));
        assertEquals(10.0, graph.getWeight("LOC-A", "LOC-B"));
        assertEquals(10.0, graph.getWeight("LOC-B", "LOC-A"));

        Edge fromA = graph.getNeighbours("LOC-A").get(0);
        Edge fromB = graph.getNeighbours("LOC-B").get(0);
        assertEquals("LOC-A", fromA.getSourceId());
        assertEquals("LOC-B", fromA.getDestinationId());
        assertEquals("LOC-B", fromB.getSourceId());
        assertEquals("LOC-A", fromB.getDestinationId());
        assertEquals(road.getDistanceKm(), fromA.getDistanceKm());
        assertEquals(road.getNormalTravelTimeMinutes(), fromA.getNormalTravelTimeMinutes());
        assertEquals(road.getRoadConditionWeight(), fromA.getRoadConditionWeight());
    }

    @Test
    void addEdgeRejectsInvalidInputsDuplicatesAndEndpointMismatches() {
        AdjacencyListGraph graph = graphWithVertices("LOC-A", "LOC-B", "LOC-C");
        Road road = road("R-1", "LOC-A", "LOC-B", 3.0, 5.0, 1.0);

        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("LOC-A", "LOC-X", road));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("LOC-A", "LOC-B", null));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge(" ", "LOC-B", road));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("LOC-A", "LOC-A", road));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("LOC-A", "LOC-C", road));

        graph.addEdge("LOC-A", "LOC-B", road);

        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("LOC-B", "LOC-A", road("R-2", "LOC-B", "LOC-A", 1.0, 1.0, 1.0)));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("LOC-B", "LOC-C", road("R-1", "LOC-B", "LOC-C", 1.0, 1.0, 1.0)));
    }

    @Test
    void neighboursAreSortedAndSnapshotsAreIndependent() {
        AdjacencyListGraph graph = graphWithVertices("LOC-A", "LOC-D", "LOC-B", "LOC-C");
        graph.addEdge("LOC-A", "LOC-D", road("R-AD", "LOC-A", "LOC-D", 1.0, 9.0, 1.0));
        graph.addEdge("LOC-A", "LOC-B", road("R-AB", "LOC-A", "LOC-B", 1.0, 7.0, 1.0));
        CustomDynamicArray<Edge> before = graph.getNeighbours("LOC-A");

        graph.addEdge("LOC-A", "LOC-C", road("R-AC", "LOC-A", "LOC-C", 1.0, 8.0, 1.0));
        before.remove(0);
        CustomDynamicArray<Edge> after = graph.getNeighbours("LOC-A");

        assertEquals(1, before.size());
        assertEquals(3, after.size());
        assertEquals("LOC-B", after.get(0).getDestinationId());
        assertEquals("LOC-C", after.get(1).getDestinationId());
        assertEquals("LOC-D", after.get(2).getDestinationId());
    }

    @Test
    void getEdgesReturnsUndirectedRoadsOnceInDeterministicOrder() {
        AdjacencyListGraph graph = graphWithVertices("LOC-A", "LOC-B", "LOC-C", "LOC-D");
        Road cd = road("R-CD", "LOC-C", "LOC-D", 1.0, 1.0, 1.0);
        Road ab = road("R-AB", "LOC-B", "LOC-A", 1.0, 1.0, 1.0);
        Road ac = road("R-AC", "LOC-A", "LOC-C", 1.0, 1.0, 1.0);
        graph.addEdge("LOC-C", "LOC-D", cd);
        graph.addEdge("LOC-A", "LOC-B", ab);
        graph.addEdge("LOC-A", "LOC-C", ac);

        CustomDynamicArray<Road> edges = graph.getEdges();
        edges.remove(0);
        CustomDynamicArray<Road> laterEdges = graph.getEdges();

        assertEquals(2, edges.size());
        assertEquals(3, laterEdges.size());
        assertSame(ab, laterEdges.get(0));
        assertSame(ac, laterEdges.get(1));
        assertSame(cd, laterEdges.get(2));
    }

    @Test
    void removeEdgeRemovesBothDirectionsAndAllowsReplacementAndRoadIdReuse() {
        AdjacencyListGraph graph = graphWithVertices("LOC-A", "LOC-B", "LOC-C");
        Road ab = road("R-AB", "LOC-A", "LOC-B", 1.0, 5.0, 2.0);
        Road bc = road("R-BC", "LOC-B", "LOC-C", 1.0, 6.0, 1.0);
        graph.addEdge("LOC-A", "LOC-B", ab);
        graph.addEdge("LOC-B", "LOC-C", bc);

        Road removed = graph.removeEdge("LOC-B", "LOC-A");

        assertSame(ab, removed);
        assertEquals(1, graph.edgeCount());
        assertFalse(graph.containsEdge("LOC-A", "LOC-B"));
        assertEquals(0, graph.getNeighbours("LOC-A").size());
        assertEquals(1, graph.getNeighbours("LOC-B").size());
        assertThrows(IllegalArgumentException.class, () -> graph.getWeight("LOC-A", "LOC-B"));
        assertThrows(IllegalArgumentException.class, () -> graph.removeEdge("LOC-A", "LOC-B"));

        Road replacement = road("R-AB", "LOC-B", "LOC-A", 2.0, 7.0, 1.5);
        graph.addEdge("LOC-A", "LOC-B", replacement);
        assertEquals(2, graph.edgeCount());
        assertSame(replacement, graph.removeEdge("LOC-A", "LOC-B"));
    }

    @Test
    void removeVertexHandlesIsolatedAndIncidentEdges() {
        AdjacencyListGraph graph = graphWithVertices("LOC-A", "LOC-B", "LOC-C", "LOC-D");
        Location isolated = graph.getVertex("LOC-D");
        graph.addEdge("LOC-A", "LOC-B", road("R-AB", "LOC-A", "LOC-B", 1.0, 5.0, 1.0));
        graph.addEdge("LOC-B", "LOC-C", road("R-BC", "LOC-B", "LOC-C", 1.0, 6.0, 1.0));

        assertSame(isolated, graph.removeVertex("LOC-D"));
        assertEquals(3, graph.vertexCount());
        assertEquals(2, graph.edgeCount());

        Location removed = graph.removeVertex("LOC-B");

        assertEquals("LOC-B", removed.getLocationId());
        assertEquals(2, graph.vertexCount());
        assertEquals(0, graph.edgeCount());
        assertFalse(graph.containsVertex("LOC-B"));
        assertEquals(0, graph.getNeighbours("LOC-A").size());
        assertEquals(0, graph.getNeighbours("LOC-C").size());
        assertThrows(IllegalArgumentException.class, () -> graph.removeVertex("LOC-B"));
    }

    @Test
    void removingOnlyVertexEmptiesGraphAndGraphCanBeReused() {
        AdjacencyListGraph graph = graphWithVertices("LOC-A");

        graph.removeVertex("LOC-A");

        assertEquals(0, graph.vertexCount());
        assertEquals(0, graph.edgeCount());
        graph.addVertex(location("LOC-B", "Bistro", LocationType.RESTAURANT));
        assertEquals(1, graph.vertexCount());
        assertTrue(graph.containsVertex("LOC-B"));
    }

    @Test
    void foodDeliveryScenarioExposesRouteReadyEdgesAndUndirectedRoadSnapshot() {
        AdjacencyListGraph graph = new AdjacencyListGraph();
        Location restaurant = location("REST-1", "Kitchen", LocationType.RESTAURANT);
        Location home = location("HOME-1", "Home", LocationType.RESIDENTIAL_AREA);
        Location junction = location("JUNC-1", "Junction", LocationType.JUNCTION);
        graph.addVertex(restaurant);
        graph.addVertex(home);
        graph.addVertex(junction);
        Road first = road("ROAD-1", "REST-1", "JUNC-1", 2.0, 4.0, 1.5);
        Road second = road("ROAD-2", "JUNC-1", "HOME-1", 3.0, 5.0, 1.2);

        graph.addEdge("REST-1", "JUNC-1", first);
        graph.addEdge("JUNC-1", "HOME-1", second);

        assertEquals(6.0, graph.getWeight("REST-1", "JUNC-1"));
        assertEquals(6.0, graph.getWeight("JUNC-1", "HOME-1"));
        Edge outgoing = graph.getNeighbours("REST-1").get(0);
        assertEquals("JUNC-1", outgoing.getDestinationId());
        assertEquals(first.getDistanceKm(), outgoing.getDistanceKm());
        assertEquals(first.getEffectiveTime(), outgoing.getEffectiveTime());
        assertEquals(2, graph.getEdges().size());
    }

    @Test
    void largerDeterministicGraphMaintainsConsistencyThroughChanges() {
        AdjacencyListGraph graph = new AdjacencyListGraph();
        for (int i = 1; i <= 20; i++) {
            graph.addVertex(location(locationId(i), "Location " + i, LocationType.JUNCTION));
        }
        for (int i = 1; i < 20; i++) {
            graph.addEdge(locationId(i), locationId(i + 1), road("R-" + i, locationId(i), locationId(i + 1), i, i + 5.0, 1.0));
        }
        graph.addEdge("LOC-001", "LOC-010", road("R-X1", "LOC-001", "LOC-010", 2.0, 10.0, 1.1));
        graph.addEdge("LOC-005", "LOC-015", road("R-X2", "LOC-005", "LOC-015", 2.0, 11.0, 1.2));

        assertEquals(20, graph.vertexCount());
        assertEquals(21, graph.edgeCount());
        assertEquals(21, graph.getEdges().size());
        assertTrue(graph.containsEdge("LOC-010", "LOC-001"));
        assertTrue(graph.containsEdge("LOC-015", "LOC-005"));
        assertEquals(11.0, graph.getWeight("LOC-001", "LOC-010"));
        assertEquals(13.2, graph.getWeight("LOC-005", "LOC-015"), 0.000001);

        graph.removeEdge("LOC-001", "LOC-010");
        graph.removeVertex("LOC-010");

        assertEquals(19, graph.vertexCount());
        assertEquals(18, graph.edgeCount());
        assertFalse(graph.containsVertex("LOC-010"));
        for (int i = 0; i < graph.getVertices().size(); i++) {
            CustomDynamicArray<Edge> neighbours = graph.getNeighbours(graph.getVertices().get(i).getLocationId());
            for (int j = 0; j < neighbours.size(); j++) {
                assertFalse(neighbours.get(j).getDestinationId().equals("LOC-010"));
            }
        }

        graph.clear();
        assertEquals(0, graph.vertexCount());
        assertEquals(0, graph.edgeCount());
        graph.addVertex(location("LOC-100", "Reuse", LocationType.LANDMARK));
        assertEquals(1, graph.vertexCount());
    }

    private AdjacencyListGraph graphWithVertices(String... ids) {
        AdjacencyListGraph graph = new AdjacencyListGraph();
        for (String id : ids) {
            graph.addVertex(location(id, id + " name", LocationType.JUNCTION));
        }
        return graph;
    }

    private Location location(String locationId, String name, LocationType type) {
        return new Location(locationId, name, "Area", type, 5.0, -1.0);
    }

    private Road road(String roadId, String fromLocationId, String toLocationId, double distanceKm, double normalTime, double conditionWeight) {
        return new Road(roadId, fromLocationId, toLocationId, distanceKm, normalTime, conditionWeight);
    }

    private String locationId(int number) {
        if (number < 10) {
            return "LOC-00" + number;
        }
        return "LOC-0" + number;
    }
}
