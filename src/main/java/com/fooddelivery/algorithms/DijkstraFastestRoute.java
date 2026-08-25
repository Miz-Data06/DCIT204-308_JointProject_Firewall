package com.fooddelivery.algorithms;

import com.fooddelivery.algorithms.result.DijkstraStep;
import com.fooddelivery.algorithms.result.RouteResult;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.graphs.Edge;
import com.fooddelivery.datastructures.hashing.CustomMap;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.Location;

/**
 * Dijkstra fastest-route algorithm using effective road travel time.
 */
public class DijkstraFastestRoute {
    /**
     * Computes the fastest route from source to destination by effective time.
     *
     * @param graph valid custom graph
     * @param sourceLocationId existing source location
     * @param destinationLocationId existing destination location
     * @return route result with reachability, path, distance, effective time and trace
     * @throws IllegalArgumentException for null/blank/missing inputs or invalid edge weights
     *
     * Preconditions: roads are undirected and weights are positive finite values. Edge cases: source equals destination
     * returns a zero-cost one-location path; unreachable destination returns explicit unreachable result.
     * Input mutation: none. Time: O(V^2 + E + d^2) using custom-array minimum scan. Space: O(V + E) trace/path.
     * Food-delivery use: select the fastest rider/request route under road-condition multipliers.
     */
    public RouteResult findRoute(CustomGraph graph, String sourceLocationId, String destinationLocationId) {
        GraphAlgorithmSupport.requireGraph(graph);
        String source = GraphAlgorithmSupport.requireLocationId(sourceLocationId, "Source location ID");
        String destination = GraphAlgorithmSupport.requireLocationId(destinationLocationId, "Destination location ID");
        GraphAlgorithmSupport.requireVertex(graph, source);
        GraphAlgorithmSupport.requireVertex(graph, destination);

        CustomDynamicArray<DijkstraStep> trace = new CustomDynamicArray<>();
        if (source.equals(destination)) {
            CustomDynamicArray<String> path = new CustomDynamicArray<>();
            path.add(source);
            return new RouteResult(true, path, 0.0, 0.0, trace);
        }

        CustomDynamicArray<Location> vertices = GraphAlgorithmSupport.sortedVertices(graph);
        CustomMap<String, Integer> indexById = new CustomMap<>();
        for (int i = 0; i < vertices.size(); i++) {
            indexById.put(vertices.get(i).getLocationId(), i);
        }

        double[] distances = new double[vertices.size()];
        String[] predecessors = new String[vertices.size()];
        boolean[] visited = new boolean[vertices.size()];
        for (int i = 0; i < distances.length; i++) {
            distances[i] = Double.POSITIVE_INFINITY;
        }
        distances[indexById.get(source)] = 0.0;

        while (true) {
            int currentIndex = selectUnvisitedMinimum(vertices, distances, visited);
            if (currentIndex == -1) {
                break;
            }

            visited[currentIndex] = true;
            String currentId = vertices.get(currentIndex).getLocationId();
            if (currentId.equals(destination)) {
                break;
            }

            CustomDynamicArray<Edge> neighbours = GraphAlgorithmSupport.sortedNeighbours(graph, currentId);
            for (int i = 0; i < neighbours.size(); i++) {
                Edge edge = neighbours.get(i);
                GraphAlgorithmSupport.validateEdgeWeight(edge);
                Integer neighbourIndex = indexById.get(edge.getDestinationId());
                if (neighbourIndex == null || visited[neighbourIndex]) {
                    continue;
                }

                double tentative = distances[currentIndex] + edge.getEffectiveTime();
                boolean update = tentative < distances[neighbourIndex]
                        || (Double.compare(tentative, distances[neighbourIndex]) == 0
                        && isBetterPredecessor(currentId, predecessors[neighbourIndex]));
                trace.add(new DijkstraStep(currentId, edge.getDestinationId(), tentative, currentId, update));
                if (update) {
                    distances[neighbourIndex] = tentative;
                    predecessors[neighbourIndex] = currentId;
                }
            }
        }

        int destinationIndex = indexById.get(destination);
        if (!Double.isFinite(distances[destinationIndex])) {
            return new RouteResult(false, new CustomDynamicArray<>(), 0.0, 0.0, trace);
        }

        CustomDynamicArray<String> path = reconstructPath(source, destination, predecessors, indexById);
        double totalDistance = totalDistance(graph, path);
        return new RouteResult(true, path, edgeEffectiveTimes(graph, path), totalDistance, distances[destinationIndex], trace);
    }

    private static int selectUnvisitedMinimum(
            CustomDynamicArray<Location> vertices,
            double[] distances,
            boolean[] visited) {
        int selected = -1;
        for (int i = 0; i < distances.length; i++) {
            if (visited[i] || !Double.isFinite(distances[i])) {
                continue;
            }
            if (selected == -1
                    || distances[i] < distances[selected]
                    || (Double.compare(distances[i], distances[selected]) == 0
                    && vertices.get(i).getLocationId().compareTo(vertices.get(selected).getLocationId()) < 0)) {
                selected = i;
            }
        }
        return selected;
    }

    private static boolean isBetterPredecessor(String candidate, String current) {
        return current == null || candidate.compareTo(current) < 0;
    }

    private static CustomDynamicArray<String> reconstructPath(
            String source,
            String destination,
            String[] predecessors,
            CustomMap<String, Integer> indexById) {
        CustomDynamicArray<String> reversed = new CustomDynamicArray<>();
        String current = destination;
        while (current != null) {
            reversed.add(current);
            if (current.equals(source)) {
                break;
            }
            current = predecessors[indexById.get(current)];
        }

        CustomDynamicArray<String> path = new CustomDynamicArray<>();
        for (int i = reversed.size() - 1; i >= 0; i--) {
            path.add(reversed.get(i));
        }
        return path;
    }

    private static double totalDistance(CustomGraph graph, CustomDynamicArray<String> path) {
        double total = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            total += GraphAlgorithmSupport.findEdge(graph, path.get(i), path.get(i + 1)).getDistanceKm();
        }
        return total;
    }

    private static CustomDynamicArray<Double> edgeEffectiveTimes(CustomGraph graph, CustomDynamicArray<String> path) {
        CustomDynamicArray<Double> edgeTimes = new CustomDynamicArray<>();
        for (int i = 0; i < path.size() - 1; i++) {
            edgeTimes.add(GraphAlgorithmSupport.findEdge(graph, path.get(i), path.get(i + 1)).getEffectiveTime());
        }
        return edgeTimes;
    }
}
