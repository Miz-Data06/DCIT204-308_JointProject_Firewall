package com.fooddelivery.algorithms;

import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;

/**
 * Shared deterministic comparison helpers for request algorithms.
 */
public final class RequestComparators {
    private RequestComparators() {
    }

    static int compareByRequestId(DeliveryRequest first, DeliveryRequest second) {
        requireRequest(first);
        requireRequest(second);
        return first.getRequestId().compareTo(second.getRequestId());
    }

    static int compare(
            DeliveryRequest first,
            DeliveryRequest second,
            RequestSortKey sortKey,
            boolean descending,
            EstimatedDeliveryTimeProvider estimatedDeliveryTimeProvider) {
        requireRequest(first);
        requireRequest(second);
        if (sortKey == null) {
            throw new IllegalArgumentException("Sort key must not be null");
        }

        int comparison = comparePrimary(first, second, sortKey, estimatedDeliveryTimeProvider);
        if (descending) {
            comparison = -comparison;
        }
        if (comparison != 0) {
            return comparison;
        }
        return first.getRequestId().compareTo(second.getRequestId());
    }

    static void validateEstimatedProvider(RequestSortKey sortKey, EstimatedDeliveryTimeProvider provider) {
        if (sortKey == null) {
            throw new IllegalArgumentException("Sort key must not be null");
        }
        if (sortKey == RequestSortKey.ESTIMATED_DELIVERY_TIME && provider == null) {
            throw new IllegalArgumentException("Estimated delivery time provider is required");
        }
    }

    static void validateEstimatedTimes(
            CustomDynamicArray<DeliveryRequest> requests,
            RequestSortKey sortKey,
            EstimatedDeliveryTimeProvider provider) {
        if (sortKey != RequestSortKey.ESTIMATED_DELIVERY_TIME) {
            return;
        }
        for (int i = 0; i < requests.size(); i++) {
            requireEstimatedTime(provider.estimatedMinutesFor(requests.get(i)));
        }
    }

    static void requireRequests(CustomDynamicArray<DeliveryRequest> requests) {
        if (requests == null) {
            throw new IllegalArgumentException("Requests must not be null");
        }
        for (int i = 0; i < requests.size(); i++) {
            requireRequest(requests.get(i));
        }
    }

    static DeliveryRequest[] copyToArray(CustomDynamicArray<DeliveryRequest> requests) {
        DeliveryRequest[] copy = new DeliveryRequest[requests.size()];
        for (int i = 0; i < requests.size(); i++) {
            copy[i] = requests.get(i);
        }
        return copy;
    }

    static void copyBack(DeliveryRequest[] source, CustomDynamicArray<DeliveryRequest> destination) {
        for (int i = 0; i < source.length; i++) {
            destination.set(i, source[i]);
        }
    }

    static int compareValues(double first, double second) {
        if (!Double.isFinite(first) || !Double.isFinite(second)) {
            throw new IllegalArgumentException("Compared numeric values must be finite");
        }
        return Double.compare(first, second);
    }

    private static int comparePrimary(
            DeliveryRequest first,
            DeliveryRequest second,
            RequestSortKey sortKey,
            EstimatedDeliveryTimeProvider estimatedDeliveryTimeProvider) {
        return switch (sortKey) {
            case REQUEST_ID -> first.getRequestId().compareTo(second.getRequestId());
            case TIME_SUBMITTED -> first.getTimeSubmitted().compareTo(second.getTimeSubmitted());
            case DEADLINE -> first.getDeadline().compareTo(second.getDeadline());
            case PRIORITY_SCORE -> compareValues(first.getPriorityScore(), second.getPriorityScore());
            case ESTIMATED_DELIVERY_TIME -> compareValues(
                    requireEstimatedTime(estimatedDeliveryTimeProvider.estimatedMinutesFor(first)),
                    requireEstimatedTime(estimatedDeliveryTimeProvider.estimatedMinutesFor(second)));
        };
    }

    private static double requireEstimatedTime(double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException("Estimated delivery time must be finite and non-negative");
        }
        return value;
    }

    private static void requireRequest(DeliveryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request entries must not be null");
        }
    }

}
