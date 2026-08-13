package com.fooddelivery.algorithms;

import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.graphs.Edge;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.Road;

final class GraphAlgorithmSupport {
    private GraphAlgorithmSupport() {
    }

    static CustomGraph requireGraph(CustomGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph must not be null");
        }
        return graph;
    }

    static String requireLocationId(String locationId, String fieldName) {
        if (locationId == null || locationId.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
        return locationId;
    }

    static void requireVertex(CustomGraph graph, String locationId) {
        if (!graph.containsVertex(locationId)) {
            throw new IllegalArgumentException("Location does not exist: " + locationId);
        }
    }

    static CustomDynamicArray<Edge> sortedNeighbours(CustomGraph graph, String locationId) {
        CustomDynamicArray<Edge> neighbours = graph.getNeighbours(locationId);
        for (int i = 1; i < neighbours.size(); i++) {
            Edge current = neighbours.get(i);
            int j = i - 1;
            while (j >= 0 && compareEdgesByDestination(neighbours.get(j), current) > 0) {
                neighbours.set(j + 1, neighbours.get(j));
                j--;
            }
            neighbours.set(j + 1, current);
        }
        return neighbours;
    }

    static CustomDynamicArray<Location> sortedVertices(CustomGraph graph) {
        CustomDynamicArray<Location> vertices = graph.getVertices();
        for (int i = 1; i < vertices.size(); i++) {
            Location current = vertices.get(i);
            int j = i - 1;
            while (j >= 0 && vertices.get(j).getLocationId().compareTo(current.getLocationId()) > 0) {
                vertices.set(j + 1, vertices.get(j));
                j--;
            }
            vertices.set(j + 1, current);
        }
        return vertices;
    }

    static Edge findEdge(CustomGraph graph, String from, String to) {
        CustomDynamicArray<Edge> neighbours = graph.getNeighbours(from);
        for (int i = 0; i < neighbours.size(); i++) {
            Edge edge = neighbours.get(i);
            if (edge.getDestinationId().equals(to)) {
                return edge;
            }
        }
        throw new IllegalArgumentException("Edge does not exist");
    }

    static void validateRoadWeight(Road road) {
        if (road == null
                || !Double.isFinite(road.getDistanceKm())
                || !Double.isFinite(road.getNormalTravelTimeMinutes())
                || !Double.isFinite(road.getRoadConditionWeight())
                || !Double.isFinite(road.getEffectiveTime())
                || road.getDistanceKm() <= 0.0
                || road.getNormalTravelTimeMinutes() <= 0.0
                || road.getRoadConditionWeight() <= 0.0
                || road.getEffectiveTime() <= 0.0) {
            throw new IllegalArgumentException("Road weights must be positive and finite");
        }
    }

    static void validateEdgeWeight(Edge edge) {
        if (edge == null
                || !Double.isFinite(edge.getDistanceKm())
                || !Double.isFinite(edge.getNormalTravelTimeMinutes())
                || !Double.isFinite(edge.getRoadConditionWeight())
                || !Double.isFinite(edge.getEffectiveTime())
                || edge.getDistanceKm() <= 0.0
                || edge.getNormalTravelTimeMinutes() <= 0.0
                || edge.getRoadConditionWeight() <= 0.0
                || edge.getEffectiveTime() <= 0.0) {
            throw new IllegalArgumentException("Edge weights must be positive and finite");
        }
    }

    private static int compareEdgesByDestination(Edge first, Edge second) {
        int comparison = first.getDestinationId().compareTo(second.getDestinationId());
        if (comparison != 0) {
            return comparison;
        }
        return first.getRoadId().compareTo(second.getRoadId());
    }
}
