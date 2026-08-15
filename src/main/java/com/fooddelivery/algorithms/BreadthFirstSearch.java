package com.fooddelivery.algorithms;

import com.fooddelivery.algorithms.result.TraversalResult;
import com.fooddelivery.algorithms.result.TraversalStep;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.graphs.Edge;
import com.fooddelivery.datastructures.hashing.CustomSet;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.datastructures.queues.CustomQueue;

/**
 * Breadth-first traversal over the public CustomGraph interface.
 */
public class BreadthFirstSearch {
    /**
     * Visits all locations reachable from a start location in BFS order.
     *
     * @param graph valid custom graph
     * @param startLocationId existing non-blank start location ID
     * @return traversal order and queue/visit trace
     * @throws IllegalArgumentException for null graph, blank start, or missing start
     *
     * Preconditions: graph exposes neighbours through its public API. Edge cases: one vertex returns itself.
     * Input mutation: none. Time: O(V + E + d^2) due neighbour sorting per visited vertex. Space: O(V + E) trace.
     * Food-delivery use: find all delivery locations reachable from a dispatch point by road hops.
     */
    public TraversalResult traverse(CustomGraph graph, String startLocationId) {
        GraphAlgorithmSupport.requireGraph(graph);
        String start = GraphAlgorithmSupport.requireLocationId(startLocationId, "Start location ID");
        GraphAlgorithmSupport.requireVertex(graph, start);

        CustomDynamicArray<String> order = new CustomDynamicArray<>();
        CustomDynamicArray<TraversalStep> trace = new CustomDynamicArray<>();
        CustomSet<String> visited = new CustomSet<>();
        CustomQueue<String> queue = new CustomQueue<>();

        visited.add(start);
        queue.enqueue(start);
        trace.add(new TraversalStep(start, "enqueue"));

        while (!queue.isEmpty()) {
            String current = queue.dequeue();
            order.add(current);
            trace.add(new TraversalStep(current, "visit"));

            CustomDynamicArray<Edge> neighbours = GraphAlgorithmSupport.sortedNeighbours(graph, current);
            for (int i = 0; i < neighbours.size(); i++) {
                Edge edge = neighbours.get(i);
                String next = edge.getDestinationId();
                if (!visited.contains(next)) {
                    visited.add(next);
                    queue.enqueue(next);
                    trace.add(new TraversalStep(next, "enqueue"));
                }
            }
        }

        return new TraversalResult(order, trace);
    }
}
