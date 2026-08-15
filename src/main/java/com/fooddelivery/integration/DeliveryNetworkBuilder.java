package com.fooddelivery.integration;

import com.fooddelivery.database.mapper.DatasetLoadResult;
import com.fooddelivery.datastructures.graphs.AdjacencyListGraph;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.Location;
import com.fooddelivery.model.Road;

public class DeliveryNetworkBuilder {
    public CustomGraph build(DatasetLoadResult dataset) {
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset must not be null");
        }
        return build(dataset.getLocations(), dataset.getRoads());
    }

    public CustomGraph build(CustomDynamicArray<Location> locations, CustomDynamicArray<Road> roads) {
        if (locations == null || roads == null) {
            throw new IllegalArgumentException("Locations and roads must not be null");
        }
        CustomGraph graph = new AdjacencyListGraph();
        for (int i = 0; i < locations.size(); i++) {
            graph.addVertex(locations.get(i));
        }
        for (int i = 0; i < roads.size(); i++) {
            Road road = roads.get(i);
            graph.addEdge(road.getFromLocationId(), road.getToLocationId(), road);
        }
        return graph;
    }
}
