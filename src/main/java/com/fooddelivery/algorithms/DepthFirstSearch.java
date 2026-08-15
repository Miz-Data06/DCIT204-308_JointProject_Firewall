package com.fooddelivery.algorithms;

import com.fooddelivery.algorithms.result.TraversalResult;
import com.fooddelivery.algorithms.result.TraversalStep;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.graphs.Edge;
import com.fooddelivery.datastructures.hashing.CustomSet;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;

/**
 * Depth-first traversal over the public CustomGraph interface.
 */
public class DepthFirstSearch {
    /**
     * Visits all locations reachable from a start location in recursive DFS order.
     *
     * @param graph valid custom graph
     * @param startLocationId existing non-blank start location ID
     * @return traversal order and recursive visit trace
     * @throws IllegalArgumentException for null graph, blank start, or missing start
     *
     * Preconditions: graph exposes neighbours through its public API. Edge cases: cycles are guarded by visited set.
     * Input mutation: none. Time: O(V + E + d^2) due neighbour sorting per visited vertex. Space: O(V) recursion/visited.
     * Food-delivery use: explore reachable service zones along each road branch before backtracking.
     */
    public TraversalResult traverse(CustomGraph graph, String startLocationId) {
        GraphAlgorithmSupport.requireGraph(graph);
        String start = GraphAlgorithmSupport.requireLocationId(startLocationId, "Start location ID");
        GraphAlgorithmSupport.requireVertex(graph, start);

        CustomDynamicArray<String> order = new CustomDynamicArray<>();
        CustomDynamicArray<TraversalStep> trace = new CustomDynamicArray<>();
        CustomSet<String> visited = new CustomSet<>();
        dfs(graph, start, visited, order, trace);
        return new TraversalResult(order, trace);
    }

    private static void dfs(
            CustomGraph graph,
            String current,
            CustomSet<String> visited,
            CustomDynamicArray<String> order,
            CustomDynamicArray<TraversalStep> trace) {
        visited.add(current);
        order.add(current);
        trace.add(new TraversalStep(current, "visit"));

        CustomDynamicArray<Edge> neighbours = GraphAlgorithmSupport.sortedNeighbours(graph, current);
        for (int i = 0; i < neighbours.size(); i++) {
            String next = neighbours.get(i).getDestinationId();
            if (!visited.contains(next)) {
                dfs(graph, next, visited, order, trace);
            }
        }
    }
}
