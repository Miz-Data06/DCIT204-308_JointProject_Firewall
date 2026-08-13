package com.fooddelivery.algorithms;

import com.fooddelivery.algorithms.result.MstResult;
import com.fooddelivery.algorithms.result.MstStep;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.hashing.CustomSet;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.Road;

/**
 * Prim minimum spanning tree/forest algorithm over CustomGraph.
 */
public class PrimMinimumSpanningTree {
    /**
     * Builds a minimum spanning tree from the requested start, then continues as a deterministic forest if disconnected.
     *
     * @param graph valid custom undirected graph
     * @param startLocationId existing non-blank start vertex
     * @return selected roads, total effective cost, component count and trace
     * @throws IllegalArgumentException for null graph, blank/missing start, or invalid edge weights
     *
     * Preconditions: graph returns each undirected road once. Edge cases: empty graph is invalid because a start is
     * required; one vertex returns no roads. Input mutation: none. Time: O(VE) with edge scans. Space: O(V + E).
     * Food-delivery use: identify a low-travel-time road network connecting service locations.
     */
    public MstResult build(CustomGraph graph, String startLocationId) {
        GraphAlgorithmSupport.requireGraph(graph);
        String start = GraphAlgorithmSupport.requireLocationId(startLocationId, "Start location ID");
        GraphAlgorithmSupport.requireVertex(graph, start);

        CustomDynamicArray<Location> vertices = GraphAlgorithmSupport.sortedVertices(graph);
        CustomSet<String> visited = new CustomSet<>();
        CustomDynamicArray<Road> selected = new CustomDynamicArray<>();
        CustomDynamicArray<MstStep> trace = new CustomDynamicArray<>();
        double totalCost = 0.0;
        int components = 0;

        totalCost = growComponent(graph, start, visited, selected, trace, totalCost);
        components++;
        for (int i = 0; i < vertices.size(); i++) {
            String locationId = vertices.get(i).getLocationId();
            if (!visited.contains(locationId)) {
                totalCost = growComponent(graph, locationId, visited, selected, trace, totalCost);
                components++;
            }
        }

        return new MstResult(selected, totalCost, graph.vertexCount(), components, trace);
    }

    private static double growComponent(
            CustomGraph graph,
            String start,
            CustomSet<String> visited,
            CustomDynamicArray<Road> selected,
            CustomDynamicArray<MstStep> trace,
            double totalCost) {
        visited.add(start);
        while (true) {
            Road best = selectCheapestFrontier(graph, visited);
            if (best == null) {
                return totalCost;
            }
            selected.add(best);
            totalCost += best.getEffectiveTime();
            String next = visited.contains(best.getFromLocationId()) ? best.getToLocationId() : best.getFromLocationId();
            visited.add(next);
            trace.add(new MstStep(best.getRoadId(), MstEdgeOrdering.normalizedFrom(best),
                    MstEdgeOrdering.normalizedTo(best), best.getEffectiveTime(), true, "frontier edge selected"));
        }
    }

    private static Road selectCheapestFrontier(CustomGraph graph, CustomSet<String> visited) {
        CustomDynamicArray<Road> roads = graph.getEdges();
        Road best = null;
        for (int i = 0; i < roads.size(); i++) {
            Road road = roads.get(i);
            GraphAlgorithmSupport.validateRoadWeight(road);
            boolean fromVisited = visited.contains(road.getFromLocationId());
            boolean toVisited = visited.contains(road.getToLocationId());
            if (fromVisited == toVisited) {
                continue;
            }
            if (best == null || MstEdgeOrdering.compare(road, best) < 0) {
                best = road;
            }
        }
        return best;
    }
}
