package com.fooddelivery.web;

import com.fooddelivery.algorithms.result.RiderAssignmentResult;
import com.fooddelivery.algorithms.result.RouteResult;
import com.fooddelivery.model.DeliveryRequest;

final class WebOrderConfirmation {
    private final DeliveryRequest request;
    private final RouteResult route;
    private final RiderAssignmentResult assignment;

    WebOrderConfirmation(DeliveryRequest request, RouteResult route, RiderAssignmentResult assignment) {
        this.request = request;
        this.route = route;
        this.assignment = assignment;
    }

    DeliveryRequest request() {
        return request;
    }

    RouteResult route() {
        return route;
    }

    RiderAssignmentResult assignment() {
        return assignment;
    }
}
