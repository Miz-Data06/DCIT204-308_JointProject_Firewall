package com.fooddelivery.datastructures.graphs;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.Road;

/**
 * Shared public contract for custom weighted delivery graphs.
 */
public interface CustomGraph {
    void addVertex(Location location);

    void addEdge(String fromLocationId, String toLocationId, Road road);

    Location removeVertex(String locationId);

    Road removeEdge(String fromLocationId, String toLocationId);

    CustomDynamicArray<Edge> getNeighbours(String locationId);

    double getWeight(String fromLocationId, String toLocationId);

    boolean containsVertex(String locationId);

    Location getVertex(String locationId);

    CustomDynamicArray<Location> getVertices();

    CustomDynamicArray<Road> getEdges();

    int vertexCount();

    int edgeCount();

    boolean containsEdge(String fromLocationId, String toLocationId);

    void clear();
}
