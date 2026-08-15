package com.fooddelivery.algorithms;

import com.fooddelivery.datastructures.graphs.AdjacencyListGraph;
import com.fooddelivery.datastructures.graphs.AdjacencyMatrixGraph;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.LocationType;
import com.fooddelivery.model.Road;

final class GraphAlgorithmFixtures {
    private GraphAlgorithmFixtures() {
    }

    static CustomDynamicArray<CustomGraph> graphRepresentations() {
        CustomDynamicArray<CustomGraph> graphs = new CustomDynamicArray<>();
        graphs.add(new AdjacencyListGraph());
        graphs.add(new AdjacencyMatrixGraph());
        return graphs;
    }

    static void addLocation(CustomGraph graph, String id) {
        graph.addVertex(new Location(id, id + " Name", "Legon", LocationType.RESTAURANT, 5.0, -0.1));
    }

    static void addRoad(CustomGraph graph, String roadId, String from, String to, double distance, double normalTime, double weight) {
        graph.addEdge(from, to, new Road(roadId, from, to, distance, normalTime, weight));
    }

    static String ids(CustomDynamicArray<String> ids) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(ids.get(i));
        }
        return builder.toString();
    }

    static CustomGraph connectedGraph(CustomGraph graph) {
        addLocation(graph, "A");
        addLocation(graph, "B");
        addLocation(graph, "C");
        addLocation(graph, "D");
        addRoad(graph, "R-AB", "A", "B", 1.0, 5.0, 1.0);
        addRoad(graph, "R-AC", "A", "C", 1.0, 4.0, 1.0);
        addRoad(graph, "R-BD", "B", "D", 1.0, 2.0, 1.0);
        addRoad(graph, "R-CD", "C", "D", 1.0, 2.0, 1.0);
        return graph;
    }

    static CustomGraph routeChoiceGraph(CustomGraph graph) {
        addLocation(graph, "A");
        addLocation(graph, "B");
        addLocation(graph, "C");
        addLocation(graph, "D");
        addRoad(graph, "R-AB", "A", "B", 1.0, 8.0, 1.0);
        addRoad(graph, "R-BD", "B", "D", 1.0, 8.0, 1.0);
        addRoad(graph, "R-AC", "A", "C", 10.0, 3.0, 1.0);
        addRoad(graph, "R-CD", "C", "D", 10.0, 3.0, 1.0);
        return graph;
    }
}
