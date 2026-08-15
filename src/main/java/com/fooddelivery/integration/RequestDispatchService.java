package com.fooddelivery.integration;

import com.fooddelivery.algorithms.GreedyRiderAssignment;
import com.fooddelivery.algorithms.result.RiderAssignmentResult;
import com.fooddelivery.database.mapper.DatasetLoadResult;
import com.fooddelivery.datastructures.graphs.CustomGraph;
import com.fooddelivery.model.DeliveryRequest;

public class RequestDispatchService {
    private final DatasetLoadResult dataset;
    private final CustomGraph graph;

    public RequestDispatchService(DatasetLoadResult dataset, CustomGraph graph) {
        if (dataset == null || graph == null) {
            throw new IllegalArgumentException("Dataset and graph must not be null");
        }
        this.dataset = dataset;
        this.graph = graph;
    }

    public RiderAssignmentResult assignFirstRequest() {
        DeliveryRequest request = dataset.getDeliveryRequests().get(0);
        return new GreedyRiderAssignment().assign(graph, dataset.getRiders(), request);
    }
}
