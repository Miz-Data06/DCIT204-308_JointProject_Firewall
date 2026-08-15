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

class AdjacencyMatrixGraphTest {
    @Test
    void constructionCapacityAndEmptySnapshotsBehaveDeterministically() {
        AdjacencyMatrixGraph defaultGraph = new AdjacencyMatrixGraph();
        AdjacencyMatrixGraph oneSlotGraph = new AdjacencyMatrixGraph(1);
        AdjacencyMatrixGraph fiveSlotGraph = new AdjacencyMatrixGraph(5);

        assertEquals(0, defaultGraph.vertexCount());
        assertEquals(0, defaultGraph.edgeCount());
        assertEquals(10, defaultGraph.capacity());
        assertEquals(1, oneSlotGraph.capacity());
        assertEquals(5, fiveSlotGraph.capacity());
        assertThrows(IllegalArgumentException.class, () -> new AdjacencyMatrixGraph(0));
        assertThrows(IllegalArgumentException.class, () -> new AdjacencyMatrixGraph(-1));

        CustomDynamicArray<Location> vertices = defaultGraph.getVertices();
        CustomDynamicArray<Road> edges = defaultGraph.getEdges();
        vertices.add(location("LOC-X", "extra", LocationType.JUNCTION));
        assertEquals(1, vertices.size());
        assertEquals(0, defaultGraph.getVertices().size());
        assertEquals(0, edges.size());

        defaultGraph.clear();
        assertEquals(10, defaultGraph.capacity());
        assertEquals(0, defaultGraph.vertexCount());
        assertEquals(0, defaultGraph.edgeCount());
    }

    @Test
    void addVertexStoresUpdatesOrdersAndPreservesEdgesWithoutResizing() {
        AdjacencyMatrixGraph graph = new AdjacencyMatrixGraph(3);
        Location c = location("LOC-C", "Campus", LocationType.CAMPUS);
        Location a = location("LOC-A", "Apartments", LocationType.RESIDENTIAL_AREA);
        Location b = location("LOC-B", "Bistro", LocationType.RESTAURANT);
        Location replacement = location("LOC-B", "Better Bistro", LocationType.RESTAURANT);
        graph.addVertex(c);
        graph.addVertex(a);
        graph.addVertex(b);
        graph.addEdge("LOC-A", "LOC-B", road("R-AB", "LOC-A", "LOC-B", 2.0, 5.0, 1.5));

        graph.addVertex(replacement);
        CustomDynamicArray<Location> beforeLaterAdd = graph.getVertices();
        graph.addVertex(location("LOC-D", "Depot", LocationType.LANDMARK));

        assertEquals(4, graph.vertexCount());
        assertEquals(1, graph.edgeCount());
        assertEquals(6, graph.capacity());
        assertSame(replacement, graph.getVertex("LOC-B"));
        assertNull(graph.getVertex("LOC-X"));
        assertTrue(graph.containsVertex("LOC-B"));
        assertTrue(graph.containsEdge("LOC-B", "LOC-A"));
        assertEquals(7.5, graph.getWeight("LOC-A", "LOC-B"));
        assertEquals(3, beforeLaterAdd.size());

        CustomDynamicArray<Location> vertices = graph.getVertices();
        assertSame(a, vertices.get(0));
        assertSame(replacement, vertices.get(1));
        assertSame(c, vertices.get(2));
        assertEquals("LOC-D", vertices.get(3).getLocationId());
        vertices.remove(0);
        assertEquals(4, graph.getVertices().size());
    }

    @Test
    void vertexValidationRejectsInvalidPublicInputs() {
        AdjacencyMatrixGraph graph = new AdjacencyMatrixGraph();

        assertThrows(IllegalArgumentException.class, () -> graph.addVertex(null));
        assertThrows(IllegalArgumentException.class, () -> graph.containsVertex(null));
        assertThrows(IllegalArgumentException.class, () -> graph.containsVertex(" "));
        assertThrows(IllegalArgumentException.class, () -> graph.getVertex(null));
        assertThrows(IllegalArgumentException.class, () -> graph.getNeighbours("LOC-X"));
        assertThrows(IllegalArgumentException.class, () -> graph.removeVertex("LOC-X"));
        assertFalse(graph.containsVertex("LOC-X"));
        assertNull(graph.getVertex("LOC-X"));
    }

    @Test
    void capacityGrowthPreservesVerticesEdgesAndAvoidsDuplicateUpdateResize() {
        AdjacencyMatrixGraph graph = new AdjacencyMatrixGraph(1);
        graph.addVertex(location("LOC-001", "one", LocationType.JUNCTION));
        assertEquals(1, graph.capacity());
        graph.addVertex(location("LOC-002", "two", LocationType.JUNCTION));
        assertEquals(2, graph.capacity());
        graph.addEdge("LOC-001", "LOC-002", road("R-1", "LOC-001", "LOC-002", 1.0, 5.0, 2.0));
        graph.addVertex(location("LOC-003", "three", LocationType.JUNCTION));
        graph.addVertex(location("LOC-004", "four", LocationType.JUNCTION));
        graph.addVertex(location("LOC-005", "five", LocationType.JUNCTION));

        assertEquals(8, graph.capacity());
        assertEquals(5, graph.vertexCount());
        assertEquals(1, graph.edgeCount());
        assertTrue(graph.containsEdge("LOC-002", "LOC-001"));
        assertEquals(10.0, graph.getWeight("LOC-001", "LOC-002"));

        graph.addVertex(location("LOC-005", "five replacement", LocationType.JUNCTION));
        assertEquals(8, graph.capacity());
        assertEquals(5, graph.vertexCount());

        graph.clear();
        assertEquals(8, graph.capacity());
        graph.addVertex(location("LOC-A", "A", LocationType.JUNCTION));
        graph.addVertex(location("LOC-B", "B", LocationType.JUNCTION));
        graph.addEdge("LOC-A", "LOC-B", road("R-AB", "LOC-A", "LOC-B", 2.0, 3.0, 4.0));
        assertEquals(12.0, graph.getWeight("LOC-B", "LOC-A"));
    }

    @Test
    void addEdgeValidatesBeforeMutationAndCreatesSymmetricOutgoingViews() {
        AdjacencyMatrixGraph graph = graphWithVertices(4, "LOC-A", "LOC-B", "LOC-C");
        Road road = road("R-AB", "LOC-A", "LOC-B", 3.5, 8.0, 1.25);

        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("LOC-A", "LOC-X", road));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("LOC-A", "LOC-B", null));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge(null, "LOC-B", road));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("LOC-A", " ", road));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("LOC-A", "LOC-A", road));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("LOC-A", "LOC-C", road));
        assertEquals(0, graph.edgeCount());

        graph.addEdge("LOC-A", "LOC-B", road);

        assertEquals(1, graph.edgeCount());
        assertTrue(graph.containsEdge("LOC-A", "LOC-B"));
        assertTrue(graph.containsEdge("LOC-B", "LOC-A"));
        assertFalse(graph.containsEdge("LOC-A", "LOC-C"));
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

        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("LOC-B", "LOC-A", road("R-BA", "LOC-B", "LOC-A", 1.0, 1.0, 1.0)));
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge("LOC-B", "LOC-C", road("R-AB", "LOC-B", "LOC-C", 1.0, 1.0, 1.0)));
        assertThrows(IllegalArgumentException.class, () -> graph.getWeight("LOC-A", "LOC-C"));
        assertEquals(1, graph.edgeCount());
    }

    @Test
    void neighbourSnapshotsAreOrderedIndependentAndSourceCorrect() {
        AdjacencyMatrixGraph graph = graphWithVertices(6, "LOC-A", "LOC-D", "LOC-B", "LOC-C", "LOC-E");
        graph.addEdge("LOC-A", "LOC-D", road("R-AD", "LOC-A", "LOC-D", 1.0, 9.0, 1.0));
        graph.addEdge("LOC-A", "LOC-B", road("R-AB", "LOC-A", "LOC-B", 1.0, 7.0, 1.0));
        assertEquals(0, graph.getNeighbours("LOC-E").size());
        CustomDynamicArray<Edge> before = graph.getNeighbours("LOC-A");

        graph.addEdge("LOC-A", "LOC-C", road("R-AC", "LOC-A", "LOC-C", 1.0, 8.0, 1.0));
        before.remove(0);
        CustomDynamicArray<Edge> after = graph.getNeighbours("LOC-A");

        assertEquals(1, before.size());
        assertEquals(3, after.size());
        assertEquals("LOC-B", after.get(0).getDestinationId());
        assertEquals("LOC-C", after.get(1).getDestinationId());
        assertEquals("LOC-D", after.get(2).getDestinationId());
        for (int i = 0; i < after.size(); i++) {
            assertEquals("LOC-A", after.get(i).getSourceId());
        }
    }

    @Test
    void roadSnapshotsReturnOriginalRoadsOnceInDeterministicOrder() {
        AdjacencyMatrixGraph graph = graphWithVertices(5, "LOC-A", "LOC-B", "LOC-C", "LOC-D");
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
    void removeEdgeCleansBothDirectionsPreservesOtherRoadsAndAllowsReuse() {
        AdjacencyMatrixGraph graph = graphWithVertices(5, "LOC-A", "LOC-B", "LOC-C");
        Road ab = road("R-AB", "LOC-A", "LOC-B", 1.0, 5.0, 2.0);
        Road bc = road("R-BC", "LOC-B", "LOC-C", 1.0, 6.0, 1.0);
        graph.addEdge("LOC-A", "LOC-B", ab);
        graph.addEdge("LOC-B", "LOC-C", bc);
        int capacity = graph.capacity();

        Road removed = graph.removeEdge("LOC-B", "LOC-A");

        assertSame(ab, removed);
        assertEquals(1, graph.edgeCount());
        assertEquals(capacity, graph.capacity());
        assertFalse(graph.containsEdge("LOC-A", "LOC-B"));
        assertFalse(graph.containsEdge("LOC-B", "LOC-A"));
        assertTrue(graph.containsEdge("LOC-B", "LOC-C"));
        assertEquals(0, graph.getNeighbours("LOC-A").size());
        assertThrows(IllegalArgumentException.class, () -> graph.getWeight("LOC-A", "LOC-B"));
        assertThrows(IllegalArgumentException.class, () -> graph.removeEdge("LOC-A", "LOC-B"));

        Road replacement = road("R-AB", "LOC-B", "LOC-A", 2.0, 7.0, 1.5);
        graph.addEdge("LOC-A", "LOC-B", replacement);
        assertEquals(2, graph.edgeCount());
        assertSame(replacement, graph.removeEdge("LOC-A", "LOC-B"));
    }

    @Test
    void removeVertexHandlesIsolatedOnlyFirstMiddleLastAndShiftedEdges() {
        AdjacencyMatrixGraph graph = graphWithVertices(8, "LOC-A", "LOC-B", "LOC-C", "LOC-D", "LOC-E", "LOC-F");
        Location isolated = graph.getVertex("LOC-F");
        graph.addEdge("LOC-A", "LOC-B", road("R-AB", "LOC-A", "LOC-B", 1.0, 5.0, 1.0));
        graph.addEdge("LOC-B", "LOC-C", road("R-BC", "LOC-B", "LOC-C", 1.0, 6.0, 1.0));
        graph.addEdge("LOC-C", "LOC-D", road("R-CD", "LOC-C", "LOC-D", 1.0, 7.0, 1.5));
        graph.addEdge("LOC-D", "LOC-E", road("R-DE", "LOC-D", "LOC-E", 1.0, 8.0, 2.0));
        int capacity = graph.capacity();

        assertSame(isolated, graph.removeVertex("LOC-F"));
        assertEquals(5, graph.vertexCount());
        assertEquals(4, graph.edgeCount());
        assertEquals(capacity, graph.capacity());

        Location removedFirst = graph.removeVertex("LOC-A");
        assertEquals("LOC-A", removedFirst.getLocationId());
        assertEquals(4, graph.vertexCount());
        assertEquals(3, graph.edgeCount());
        assertFalse(graph.containsVertex("LOC-A"));
        assertNull(graph.getVertex("LOC-A"));
        assertTrue(graph.containsEdge("LOC-B", "LOC-C"));

        Location removedMiddle = graph.removeVertex("LOC-C");
        assertEquals("LOC-C", removedMiddle.getLocationId());
        assertEquals(3, graph.vertexCount());
        assertEquals(1, graph.edgeCount());
        assertTrue(graph.containsEdge("LOC-D", "LOC-E"));
        assertEquals(16.0, graph.getWeight("LOC-E", "LOC-D"));

        Location removedLast = graph.removeVertex("LOC-E");
        assertEquals("LOC-E", removedLast.getLocationId());
        assertEquals(2, graph.vertexCount());
        assertEquals(0, graph.edgeCount());
        assertEquals(capacity, graph.capacity());
        assertThrows(IllegalArgumentException.class, () -> graph.removeVertex("LOC-Z"));
        for (int i = 0; i < graph.getVertices().size(); i++) {
            CustomDynamicArray<Edge> neighbours = graph.getNeighbours(graph.getVertices().get(i).getLocationId());
            for (int j = 0; j < neighbours.size(); j++) {
                assertFalse(neighbours.get(j).getDestinationId().equals("LOC-C"));
            }
        }

        graph.addVertex(location("LOC-A", "A again", LocationType.JUNCTION));
        graph.addVertex(location("LOC-G", "G", LocationType.JUNCTION));
        graph.addEdge("LOC-A", "LOC-G", road("R-AG", "LOC-A", "LOC-G", 2.0, 4.0, 1.25));
        assertTrue(graph.containsEdge("LOC-G", "LOC-A"));
    }

    @Test
    void removingOnlyVertexEmptiesGraphAndGraphCanBeReused() {
        AdjacencyMatrixGraph graph = graphWithVertices(2, "LOC-A");

        Location removed = graph.removeVertex("LOC-A");

        assertEquals("LOC-A", removed.getLocationId());
        assertEquals(0, graph.vertexCount());
        assertEquals(0, graph.edgeCount());
        graph.addVertex(location("LOC-B", "Bistro", LocationType.RESTAURANT));
        assertEquals(1, graph.vertexCount());
        assertTrue(graph.containsVertex("LOC-B"));
    }

    @Test
    void clearRemovesLogicalStatePreservesCapacityAndAllowsReuseAndGrowth() {
        AdjacencyMatrixGraph graph = graphWithVertices(2, "LOC-A", "LOC-B");
        graph.addEdge("LOC-A", "LOC-B", road("R-AB", "LOC-A", "LOC-B", 2.0, 3.0, 2.0));
        graph.addVertex(location("LOC-C", "C", LocationType.JUNCTION));
        int capacity = graph.capacity();

        graph.clear();

        assertEquals(0, graph.vertexCount());
        assertEquals(0, graph.edgeCount());
        assertEquals(capacity, graph.capacity());
        assertFalse(graph.containsVertex("LOC-A"));
        assertNull(graph.getVertex("LOC-A"));
        assertThrows(IllegalArgumentException.class, () -> graph.containsEdge("LOC-A", "LOC-B"));
        assertThrows(IllegalArgumentException.class, () -> graph.getWeight("LOC-A", "LOC-B"));

        graph.addVertex(location("LOC-A", "A", LocationType.JUNCTION));
        graph.addVertex(location("LOC-B", "B", LocationType.JUNCTION));
        graph.addEdge("LOC-A", "LOC-B", road("R-AB", "LOC-A", "LOC-B", 1.0, 2.0, 3.0));
        graph.addVertex(location("LOC-C", "C", LocationType.JUNCTION));
        graph.addVertex(location("LOC-D", "D", LocationType.JUNCTION));
        graph.addVertex(location("LOC-E", "E", LocationType.JUNCTION));
        assertEquals(6.0, graph.getWeight("LOC-B", "LOC-A"));
        assertTrue(graph.capacity() >= capacity);
    }

    @Test
    void publicResultsMatchAdjacencyListGraphForEquivalentOperations() {
        CustomGraph matrix = new AdjacencyMatrixGraph(2);
        CustomGraph list = new AdjacencyListGraph();
        addToBoth(matrix, list, location("LOC-C", "C", LocationType.CAMPUS));
        addToBoth(matrix, list, location("LOC-A", "A", LocationType.RESTAURANT));
        addToBoth(matrix, list, location("LOC-B", "B", LocationType.RESIDENTIAL_AREA));
        addToBoth(matrix, list, location("LOC-D", "D", LocationType.JUNCTION));
        addRoadToBoth(matrix, list, road("R-AB", "LOC-A", "LOC-B", 1.0, 4.0, 1.5));
        addRoadToBoth(matrix, list, road("R-AC", "LOC-A", "LOC-C", 2.0, 5.0, 1.2));
        addRoadToBoth(matrix, list, road("R-CD", "LOC-C", "LOC-D", 3.0, 6.0, 1.1));

        assertEquivalentPublicState(matrix, list, "LOC-A", "LOC-B", "LOC-C", "LOC-D");
        assertThrows(IllegalArgumentException.class, () -> matrix.addEdge("LOC-A", "LOC-B", road("R-X", "LOC-A", "LOC-B", 1.0, 1.0, 1.0)));
        assertThrows(IllegalArgumentException.class, () -> list.addEdge("LOC-A", "LOC-B", road("R-X", "LOC-A", "LOC-B", 1.0, 1.0, 1.0)));

        assertEquals(list.removeEdge("LOC-B", "LOC-A").getRoadId(), matrix.removeEdge("LOC-B", "LOC-A").getRoadId());
        assertEquivalentPublicState(matrix, list, "LOC-A", "LOC-B", "LOC-C", "LOC-D");
        assertEquals(list.removeVertex("LOC-C").getLocationId(), matrix.removeVertex("LOC-C").getLocationId());
        assertEquivalentPublicState(matrix, list, "LOC-A", "LOC-B", "LOC-D");
        assertThrows(IllegalArgumentException.class, () -> matrix.getNeighbours("LOC-C"));
        assertThrows(IllegalArgumentException.class, () -> list.getNeighbours("LOC-C"));
    }

    @Test
    void foodDeliveryScenarioSurvivesMatrixGrowth() {
        AdjacencyMatrixGraph graph = new AdjacencyMatrixGraph(2);
        graph.addVertex(location("REST-1", "Kitchen", LocationType.RESTAURANT));
        graph.addVertex(location("HOME-1", "Home", LocationType.RESIDENTIAL_AREA));
        graph.addVertex(location("CAMP-1", "Campus", LocationType.CAMPUS));
        graph.addVertex(location("JUNC-1", "Junction", LocationType.JUNCTION));
        Road first = road("ROAD-1", "REST-1", "JUNC-1", 2.0, 4.0, 1.5);
        Road second = road("ROAD-2", "JUNC-1", "HOME-1", 3.0, 5.0, 1.2);
        Road third = road("ROAD-3", "CAMP-1", "JUNC-1", 1.5, 3.0, 2.0);

        graph.addEdge("REST-1", "JUNC-1", first);
        graph.addEdge("JUNC-1", "HOME-1", second);
        graph.addEdge("CAMP-1", "JUNC-1", third);

        assertEquals(4, graph.vertexCount());
        assertEquals(3, graph.edgeCount());
        assertTrue(graph.capacity() >= 4);
        assertEquals(6.0, graph.getWeight("REST-1", "JUNC-1"));
        assertEquals(6.0, graph.getWeight("JUNC-1", "HOME-1"));
        assertEquals(6.0, graph.getWeight("JUNC-1", "CAMP-1"));
        Edge outgoing = graph.getNeighbours("REST-1").get(0);
        assertEquals("REST-1", outgoing.getSourceId());
        assertEquals("JUNC-1", outgoing.getDestinationId());
        assertEquals(first.getDistanceKm(), outgoing.getDistanceKm());
        assertEquals(first.getEffectiveTime(), outgoing.getEffectiveTime());
        assertEquals(3, graph.getEdges().size());
    }

    @Test
    void largerDeterministicGraphMaintainsConsistencyThroughGrowthShiftsClearAndReuse() {
        AdjacencyMatrixGraph graph = new AdjacencyMatrixGraph(2);
        for (int i = 1; i <= 25; i++) {
            graph.addVertex(location(locationId(i), "Location " + i, LocationType.JUNCTION));
        }
        for (int i = 1; i < 25; i++) {
            graph.addEdge(locationId(i), locationId(i + 1), road("R-" + i, locationId(i), locationId(i + 1), i, i + 5.0, 1.0));
        }
        graph.addEdge("LOC-001", "LOC-010", road("R-X1", "LOC-001", "LOC-010", 2.0, 10.0, 1.1));
        graph.addEdge("LOC-005", "LOC-015", road("R-X2", "LOC-005", "LOC-015", 2.0, 11.0, 1.2));

        assertEquals(25, graph.vertexCount());
        assertEquals(26, graph.edgeCount());
        assertEquals(26, graph.getEdges().size());
        assertTrue(graph.containsEdge("LOC-010", "LOC-001"));
        assertTrue(graph.containsEdge("LOC-015", "LOC-005"));
        assertEquals(11.0, graph.getWeight("LOC-001", "LOC-010"));
        assertEquals(13.2, graph.getWeight("LOC-005", "LOC-015"), 0.000001);
        assertTrue(graph.capacity() >= 25);

        graph.removeVertex("LOC-001");
        graph.removeVertex("LOC-013");
        graph.removeVertex("LOC-025");

        assertEquals(22, graph.vertexCount());
        assertEquals(21, graph.edgeCount());
        assertTrue(graph.containsEdge("LOC-005", "LOC-015"));
        assertEquals(13.2, graph.getWeight("LOC-015", "LOC-005"), 0.000001);
        for (int i = 0; i < graph.getVertices().size(); i++) {
            CustomDynamicArray<Edge> neighbours = graph.getNeighbours(graph.getVertices().get(i).getLocationId());
            for (int j = 0; j < neighbours.size(); j++) {
                String destinationId = neighbours.get(j).getDestinationId();
                assertFalse(destinationId.equals("LOC-001"));
                assertFalse(destinationId.equals("LOC-013"));
                assertFalse(destinationId.equals("LOC-025"));
            }
        }

        graph.clear();
        assertEquals(0, graph.vertexCount());
        assertEquals(0, graph.edgeCount());
        graph.addVertex(location("LOC-100", "Reuse", LocationType.LANDMARK));
        graph.addVertex(location("LOC-101", "Reuse 2", LocationType.JUNCTION));
        graph.addEdge("LOC-100", "LOC-101", road("R-100", "LOC-100", "LOC-101", 1.0, 2.0, 2.0));
        assertEquals(4.0, graph.getWeight("LOC-101", "LOC-100"));
    }

    private AdjacencyMatrixGraph graphWithVertices(int initialCapacity, String... ids) {
        AdjacencyMatrixGraph graph = new AdjacencyMatrixGraph(initialCapacity);
        for (String id : ids) {
            graph.addVertex(location(id, id + " name", LocationType.JUNCTION));
        }
        return graph;
    }

    private void addToBoth(CustomGraph first, CustomGraph second, Location location) {
        first.addVertex(location);
        second.addVertex(location);
    }

    private void addRoadToBoth(CustomGraph first, CustomGraph second, Road road) {
        first.addEdge(road.getFromLocationId(), road.getToLocationId(), road);
        second.addEdge(road.getFromLocationId(), road.getToLocationId(), road);
    }

    private void assertEquivalentPublicState(CustomGraph matrix, CustomGraph list, String... activeIds) {
        assertEquals(list.vertexCount(), matrix.vertexCount());
        assertEquals(list.edgeCount(), matrix.edgeCount());
        assertLocationsEqual(list.getVertices(), matrix.getVertices());
        assertRoadsEqual(list.getEdges(), matrix.getEdges());
        for (String id : activeIds) {
            assertEquals(list.containsVertex(id), matrix.containsVertex(id));
            assertEdgesEqual(list.getNeighbours(id), matrix.getNeighbours(id));
        }
        for (int i = 0; i < activeIds.length; i++) {
            for (int j = i + 1; j < activeIds.length; j++) {
                boolean expected = list.containsEdge(activeIds[i], activeIds[j]);
                assertEquals(expected, matrix.containsEdge(activeIds[i], activeIds[j]));
                assertEquals(expected, matrix.containsEdge(activeIds[j], activeIds[i]));
                if (expected) {
                    assertEquals(list.getWeight(activeIds[i], activeIds[j]), matrix.getWeight(activeIds[i], activeIds[j]));
                    assertEquals(list.getWeight(activeIds[j], activeIds[i]), matrix.getWeight(activeIds[j], activeIds[i]));
                }
            }
        }
    }

    private void assertLocationsEqual(CustomDynamicArray<Location> expected, CustomDynamicArray<Location> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getLocationId(), actual.get(i).getLocationId());
        }
    }

    private void assertRoadsEqual(CustomDynamicArray<Road> expected, CustomDynamicArray<Road> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getRoadId(), actual.get(i).getRoadId());
            assertSame(expected.get(i), actual.get(i));
        }
    }

    private void assertEdgesEqual(CustomDynamicArray<Edge> expected, CustomDynamicArray<Edge> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getRoadId(), actual.get(i).getRoadId());
            assertEquals(expected.get(i).getSourceId(), actual.get(i).getSourceId());
            assertEquals(expected.get(i).getDestinationId(), actual.get(i).getDestinationId());
            assertEquals(expected.get(i).getDistanceKm(), actual.get(i).getDistanceKm());
            assertEquals(expected.get(i).getNormalTravelTimeMinutes(), actual.get(i).getNormalTravelTimeMinutes());
            assertEquals(expected.get(i).getRoadConditionWeight(), actual.get(i).getRoadConditionWeight());
            assertEquals(expected.get(i).getEffectiveTime(), actual.get(i).getEffectiveTime());
        }
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
