package com.fooddelivery.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fooddelivery.algorithms.result.RouteResult;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;

class DijkstraFastestRouteTest {
    @Test
    void fastestRouteUsesEffectiveTimeRatherThanPhysicalDistanceOnBothRepresentations() {
        CustomDynamicArray<CustomGraph> graphs = GraphAlgorithmFixtures.graphRepresentations();
        for (int i = 0; i < graphs.size(); i++) {
            CustomGraph graph = GraphAlgorithmFixtures.routeChoiceGraph(graphs.get(i));

            RouteResult result = new DijkstraFastestRoute().findRoute(graph, "A", "D");

            assertTrue(result.isReachable());
            assertEquals("A,C,D", GraphAlgorithmFixtures.ids(result.getPath()));
            assertEquals(20.0, result.getTotalDistanceKm(), 0.0001);
            assertEquals(6.0, result.getTotalEffectiveTime(), 0.0001);
            assertTrue(result.getTrace().size() > 0);
        }
    }

    @Test
    void routeReflectsRoadConditionWeight() {
        CustomGraph graph = GraphAlgorithmFixtures.graphRepresentations().get(0);
        GraphAlgorithmFixtures.addLocation(graph, "A");
        GraphAlgorithmFixtures.addLocation(graph, "B");
        GraphAlgorithmFixtures.addLocation(graph, "C");
        GraphAlgorithmFixtures.addRoad(graph, "R-AB", "A", "B", 1.0, 2.0, 10.0);
        GraphAlgorithmFixtures.addRoad(graph, "R-AC", "A", "C", 5.0, 3.0, 1.0);
        GraphAlgorithmFixtures.addRoad(graph, "R-CB", "C", "B", 5.0, 3.0, 1.0);

        RouteResult result = new DijkstraFastestRoute().findRoute(graph, "A", "B");

        assertEquals("A,C,B", GraphAlgorithmFixtures.ids(result.getPath()));
        assertEquals(6.0, result.getTotalEffectiveTime(), 0.0001);
    }

    @Test
    void equalCostRouteUsesLexicographicTieBreaking() {
        CustomGraph graph = GraphAlgorithmFixtures.connectedGraph(GraphAlgorithmFixtures.graphRepresentations().get(0));

        RouteResult result = new DijkstraFastestRoute().findRoute(graph, "A", "D");

        assertEquals("A,C,D", GraphAlgorithmFixtures.ids(result.getPath()));
        assertEquals(6.0, result.getTotalEffectiveTime(), 0.0001);
    }

    @Test
    void sourceEqualsDestinationReturnsZeroCostOneLocationPath() {
        CustomGraph graph = GraphAlgorithmFixtures.connectedGraph(GraphAlgorithmFixtures.graphRepresentations().get(0));

        RouteResult result = new DijkstraFastestRoute().findRoute(graph, "A", "A");

        assertTrue(result.isReachable());
        assertEquals("A", GraphAlgorithmFixtures.ids(result.getPath()));
        assertEquals(0.0, result.getTotalDistanceKm(), 0.0001);
        assertEquals(0.0, result.getTotalEffectiveTime(), 0.0001);
    }

    @Test
    void disconnectedDestinationReturnsExplicitUnreachableResult() {
        CustomGraph graph = GraphAlgorithmFixtures.graphRepresentations().get(0);
        GraphAlgorithmFixtures.addLocation(graph, "A");
        GraphAlgorithmFixtures.addLocation(graph, "B");

        RouteResult result = new DijkstraFastestRoute().findRoute(graph, "A", "B");

        assertFalse(result.isReachable());
        assertEquals(0, result.getPath().size());
    }

    @Test
    void dijkstraRejectsNullBlankAndMissingLocations() {
        CustomGraph graph = GraphAlgorithmFixtures.connectedGraph(GraphAlgorithmFixtures.graphRepresentations().get(0));

        assertThrows(IllegalArgumentException.class, () -> new DijkstraFastestRoute().findRoute(null, "A", "B"));
        assertThrows(IllegalArgumentException.class, () -> new DijkstraFastestRoute().findRoute(graph, null, "B"));
        assertThrows(IllegalArgumentException.class, () -> new DijkstraFastestRoute().findRoute(graph, "A", " "));
        assertThrows(IllegalArgumentException.class, () -> new DijkstraFastestRoute().findRoute(graph, "A", "Z"));
    }

    @Test
    void routeSearchDoesNotMutateGraph() {
        CustomGraph graph = GraphAlgorithmFixtures.routeChoiceGraph(GraphAlgorithmFixtures.graphRepresentations().get(0));

        new DijkstraFastestRoute().findRoute(graph, "A", "D");

        assertEquals(4, graph.vertexCount());
        assertEquals(4, graph.edgeCount());
    }
}
