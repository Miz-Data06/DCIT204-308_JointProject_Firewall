package com.fooddelivery.algorithms;

import com.fooddelivery.algorithms.result.MstResult;
import com.fooddelivery.algorithms.result.MstStep;
import com.fooddelivery.datastructures.graphs.CustomDisjointSet;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.Road;

/**
 * Kruskal minimum spanning tree/forest algorithm using CustomDisjointSet.
 */
public class KruskalMinimumSpanningTree {
    /**
     * Builds a minimum spanning tree or forest using sorted roads and custom disjoint-set connectivity checks.
     *
     * @param graph valid custom undirected graph
     * @return selected roads, total effective cost, component count and accept/reject trace
     * @throws IllegalArgumentException for null graph or invalid edge weights
     *
     * Preconditions: graph returns each undirected road once. Edge cases: empty graph returns an empty forest; one vertex
     * returns no roads. Input mutation: none. Time: O(E^2 + E alpha(V)) due insertion-sort edge ordering. Space: O(V + E).
     * Food-delivery use: choose a low-cost service-road backbone while avoiding cycles.
     */
    public MstResult build(CustomGraph graph) {
        GraphAlgorithmSupport.requireGraph(graph);
        CustomDynamicArray<Location> vertices = GraphAlgorithmSupport.sortedVertices(graph);
        CustomDisjointSet<String> disjointSet = new CustomDisjointSet<>();
        for (int i = 0; i < vertices.size(); i++) {
            disjointSet.makeSet(vertices.get(i).getLocationId());
        }

        Road[] roads = copyRoads(graph.getEdges());
        MstEdgeOrdering.sort(roads);

        CustomDynamicArray<Road> selected = new CustomDynamicArray<>();
        CustomDynamicArray<MstStep> trace = new CustomDynamicArray<>();
        double totalCost = 0.0;
        for (Road road : roads) {
            boolean accepted = disjointSet.union(road.getFromLocationId(), road.getToLocationId());
            trace.add(new MstStep(road.getRoadId(), MstEdgeOrdering.normalizedFrom(road),
                    MstEdgeOrdering.normalizedTo(road), road.getEffectiveTime(), accepted,
                    accepted ? "accepted by union" : "rejected cycle"));
            if (accepted) {
                selected.add(road);
                totalCost += road.getEffectiveTime();
                if (selected.size() == Math.max(0, graph.vertexCount() - 1)) {
                    break;
                }
            }
        }

        return new MstResult(selected, totalCost, graph.vertexCount(), disjointSet.setCount(), trace);
    }

    private static Road[] copyRoads(CustomDynamicArray<Road> edges) {
        Road[] roads = new Road[edges.size()];
        for (int i = 0; i < edges.size(); i++) {
            Road road = edges.get(i);
            GraphAlgorithmSupport.validateRoadWeight(road);
            roads[i] = road;
        }
        return roads;
    }
}
