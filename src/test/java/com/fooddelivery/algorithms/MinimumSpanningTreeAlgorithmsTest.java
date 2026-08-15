package com.fooddelivery.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fooddelivery.algorithms.result.MstResult;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.Road;

class MinimumSpanningTreeAlgorithmsTest {
    @Test
    void primAndKruskalProduceSameCostForConnectedGraphsOnBothRepresentations() {
        CustomDynamicArray<CustomGraph> graphs = GraphAlgorithmFixtures.graphRepresentations();
        for (int i = 0; i < graphs.size(); i++) {
            CustomGraph graph = weightedConnectedGraph(graphs.get(i));

            MstResult prim = new PrimMinimumSpanningTree().build(graph, "A");
            MstResult kruskal = new KruskalMinimumSpanningTree().build(graph);

            assertTrue(prim.isConnectedTree());
            assertTrue(kruskal.isConnectedTree());
            assertEquals(graph.vertexCount() - 1, prim.getSelectedRoads().size());
            assertEquals(graph.vertexCount() - 1, kruskal.getSelectedRoads().size());
            assertEquals(prim.getTotalEffectiveCost(), kruskal.getTotalEffectiveCost(), 0.0001);
            assertEquals(6.0, kruskal.getTotalEffectiveCost(), 0.0001);
        }
    }

    @Test
    void mstTieBreakingProducesDeterministicSelectedEdges() {
        CustomGraph graph = GraphAlgorithmFixtures.graphRepresentations().get(0);
        GraphAlgorithmFixtures.addLocation(graph, "A");
        GraphAlgorithmFixtures.addLocation(graph, "B");
        GraphAlgorithmFixtures.addLocation(graph, "C");
        GraphAlgorithmFixtures.addRoad(graph, "R-AC", "A", "C", 1.0, 1.0, 1.0);
        GraphAlgorithmFixtures.addRoad(graph, "R-AB", "A", "B", 1.0, 1.0, 1.0);
        GraphAlgorithmFixtures.addRoad(graph, "R-BC", "B", "C", 1.0, 1.0, 1.0);

        MstResult kruskal = new KruskalMinimumSpanningTree().build(graph);

        assertEquals("R-AB,R-AC", roadIds(kruskal.getSelectedRoads()));
        assertEquals(2.0, kruskal.getTotalEffectiveCost(), 0.0001);
        assertTrue(kruskal.getTrace().size() >= 2);
    }

    @Test
    void disconnectedGraphProducesForestInsteadOfCompleteTree() {
        CustomGraph graph = GraphAlgorithmFixtures.graphRepresentations().get(0);
        GraphAlgorithmFixtures.addLocation(graph, "A");
        GraphAlgorithmFixtures.addLocation(graph, "B");
        GraphAlgorithmFixtures.addLocation(graph, "C");
        GraphAlgorithmFixtures.addLocation(graph, "D");
        GraphAlgorithmFixtures.addRoad(graph, "R-AB", "A", "B", 1.0, 2.0, 1.0);
        GraphAlgorithmFixtures.addRoad(graph, "R-CD", "C", "D", 1.0, 3.0, 1.0);

        MstResult prim = new PrimMinimumSpanningTree().build(graph, "A");
        MstResult kruskal = new KruskalMinimumSpanningTree().build(graph);

        assertTrue(prim.isForest());
        assertTrue(kruskal.isForest());
        assertEquals(2, prim.getComponentCount());
        assertEquals(2, kruskal.getComponentCount());
        assertEquals(2, kruskal.getSelectedRoads().size());
    }

    @Test
    void oneAndTwoVertexGraphsHaveExpectedMstSizes() {
        CustomGraph one = GraphAlgorithmFixtures.graphRepresentations().get(0);
        GraphAlgorithmFixtures.addLocation(one, "A");

        MstResult oneResult = new KruskalMinimumSpanningTree().build(one);

        assertEquals(0, oneResult.getSelectedRoads().size());
        assertEquals(1, oneResult.getComponentCount());

        CustomGraph two = GraphAlgorithmFixtures.graphRepresentations().get(0);
        GraphAlgorithmFixtures.addLocation(two, "A");
        GraphAlgorithmFixtures.addLocation(two, "B");
        GraphAlgorithmFixtures.addRoad(two, "R-AB", "A", "B", 2.0, 4.0, 1.0);

        MstResult twoResult = new PrimMinimumSpanningTree().build(two, "A");

        assertEquals(1, twoResult.getSelectedRoads().size());
        assertEquals(4.0, twoResult.getTotalEffectiveCost(), 0.0001);
    }

    @Test
    void kruskalReturnsEmptyForestForEmptyGraph() {
        MstResult result = new KruskalMinimumSpanningTree().build(GraphAlgorithmFixtures.graphRepresentations().get(0));

        assertEquals(0, result.getVertexCount());
        assertEquals(0, result.getSelectedRoads().size());
        assertTrue(result.isForest());
    }

    @Test
    void primRejectsInvalidStartAndNullGraph() {
        CustomGraph graph = weightedConnectedGraph(GraphAlgorithmFixtures.graphRepresentations().get(0));

        assertThrows(IllegalArgumentException.class, () -> new PrimMinimumSpanningTree().build(null, "A"));
        assertThrows(IllegalArgumentException.class, () -> new PrimMinimumSpanningTree().build(graph, ""));
        assertThrows(IllegalArgumentException.class, () -> new PrimMinimumSpanningTree().build(graph, "Z"));
    }

    @Test
    void mstAlgorithmsDoNotMutateGraphAndKruskalRejectsCyclesInTrace() {
        CustomGraph graph = cycleBeforeFinalConnectionGraph(GraphAlgorithmFixtures.graphRepresentations().get(0));

        MstResult result = new KruskalMinimumSpanningTree().build(graph);

        assertEquals(4, graph.vertexCount());
        assertEquals(4, graph.edgeCount());
        assertTrue(hasRejectedCycle(result));
    }

    private static CustomGraph weightedConnectedGraph(CustomGraph graph) {
        GraphAlgorithmFixtures.addLocation(graph, "A");
        GraphAlgorithmFixtures.addLocation(graph, "B");
        GraphAlgorithmFixtures.addLocation(graph, "C");
        GraphAlgorithmFixtures.addLocation(graph, "D");
        GraphAlgorithmFixtures.addRoad(graph, "R-AB", "A", "B", 1.0, 1.0, 1.0);
        GraphAlgorithmFixtures.addRoad(graph, "R-AC", "A", "C", 1.0, 2.0, 1.0);
        GraphAlgorithmFixtures.addRoad(graph, "R-BC", "B", "C", 1.0, 5.0, 1.0);
        GraphAlgorithmFixtures.addRoad(graph, "R-BD", "B", "D", 1.0, 3.0, 1.0);
        GraphAlgorithmFixtures.addRoad(graph, "R-CD", "C", "D", 1.0, 3.0, 1.0);
        return graph;
    }

    private static CustomGraph cycleBeforeFinalConnectionGraph(CustomGraph graph) {
        GraphAlgorithmFixtures.addLocation(graph, "A");
        GraphAlgorithmFixtures.addLocation(graph, "B");
        GraphAlgorithmFixtures.addLocation(graph, "C");
        GraphAlgorithmFixtures.addLocation(graph, "D");
        GraphAlgorithmFixtures.addRoad(graph, "R-AB", "A", "B", 1.0, 1.0, 1.0);
        GraphAlgorithmFixtures.addRoad(graph, "R-AC", "A", "C", 1.0, 1.0, 1.0);
        GraphAlgorithmFixtures.addRoad(graph, "R-BC", "B", "C", 1.0, 1.0, 1.0);
        GraphAlgorithmFixtures.addRoad(graph, "R-CD", "C", "D", 1.0, 10.0, 1.0);
        return graph;
    }

    private static String roadIds(CustomDynamicArray<Road> roads) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < roads.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(roads.get(i).getRoadId());
        }
        return builder.toString();
    }

    private static boolean hasRejectedCycle(MstResult result) {
        for (int i = 0; i < result.getTrace().size(); i++) {
            if (!result.getTrace().get(i).isSelected()) {
                return true;
            }
        }
        return false;
    }
}
