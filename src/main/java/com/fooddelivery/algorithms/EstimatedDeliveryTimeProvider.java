package com.fooddelivery.algorithms;

import com.fooddelivery.model.DeliveryRequest;

/**
 * Supplies caller-owned estimated delivery times without changing the shared request model.
 */
@FunctionalInterface
public interface EstimatedDeliveryTimeProvider {
    double estimatedMinutesFor(DeliveryRequest request);
}
