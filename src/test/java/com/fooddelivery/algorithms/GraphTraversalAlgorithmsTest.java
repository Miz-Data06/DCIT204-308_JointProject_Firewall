package com.fooddelivery.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fooddelivery.algorithms.result.TraversalResult;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;

class GraphTraversalAlgorithmsTest {
    @Test
    void breadthFirstSearchUsesLexicographicNeighbourOrderOnBothRepresentations() {
        CustomDynamicArray<CustomGraph> graphs = GraphAlgorithmFixtures.graphRepresentations();
        for (int i = 0; i < graphs.size(); i++) {
            CustomGraph graph = GraphAlgorithmFixtures.connectedGraph(graphs.get(i));

            TraversalResult result = new BreadthFirstSearch().traverse(graph, "A");

            assertEquals("A,B,C,D", GraphAlgorithmFixtures.ids(result.getOrder()));
            assertTrue(result.getTrace().size() >= 4);
        }
    }

    @Test
    void depthFirstSearchUsesLexicographicOrderAndHandlesCyclesOnBothRepresentations() {
        CustomDynamicArray<CustomGraph> graphs = GraphAlgorithmFixtures.graphRepresentations();
        for (int i = 0; i < graphs.size(); i++) {
            CustomGraph graph = GraphAlgorithmFixtures.connectedGraph(graphs.get(i));

            TraversalResult result = new DepthFirstSearch().traverse(graph, "A");

            assertEquals("A,B,D,C", GraphAlgorithmFixtures.ids(result.getOrder()));
        }
    }

    @Test
    void traversalsRejectMissingAndInvalidStarts() {
        CustomGraph graph = GraphAlgorithmFixtures.connectedGraph(GraphAlgorithmFixtures.graphRepresentations().get(0));

        assertThrows(IllegalArgumentException.class, () -> new BreadthFirstSearch().traverse(null, "A"));
        assertThrows(IllegalArgumentException.class, () -> new BreadthFirstSearch().traverse(graph, " "));
        assertThrows(IllegalArgumentException.class, () -> new DepthFirstSearch().traverse(graph, "Z"));
    }

    @Test
    void traversalOfOneVertexReturnsOnlyThatVertexAndDoesNotMutateGraph() {
        CustomGraph graph = GraphAlgorithmFixtures.graphRepresentations().get(0);
        GraphAlgorithmFixtures.addLocation(graph, "A");

        TraversalResult bfs = new BreadthFirstSearch().traverse(graph, "A");
        TraversalResult dfs = new DepthFirstSearch().traverse(graph, "A");

        assertEquals("A", GraphAlgorithmFixtures.ids(bfs.getOrder()));
        assertEquals("A", GraphAlgorithmFixtures.ids(dfs.getOrder()));
        assertEquals(1, graph.vertexCount());
        assertEquals(0, graph.edgeCount());
    }
}
