package com.fooddelivery.algorithms;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.algorithms.result.SortResult;
import com.fooddelivery.algorithms.result.SortTraceStep;

/**
 * Selection sort for delivery requests using deterministic requestId tie-breaking.
 */
public class SelectionSort {
    /**
     * Returns a sorted copy of the requests using selection sort.
     *
     * @param requests valid custom request array
     * @param sortKey selected request field
     * @return copied sorted result with operation count and trace
     * @throws IllegalArgumentException for null input, null key, null entries, or missing estimated-time provider
     *
     * Preconditions: estimated-time sorting needs a provider. Edge cases: empty and one-item inputs succeed.
     * Input mutation: none. Time: best/average/worst O(n^2). Space: O(n) copied result plus trace.
     * Food-delivery use: simple demonstrable ordering of small request sets.
     */
    public SortResult sort(CustomDynamicArray<DeliveryRequest> requests, RequestSortKey sortKey) {
        return sort(requests, sortKey, false, null);
    }

    /**
     * Returns a sorted copy, optionally descending for priority dispatch or other selected keys.
     */
    public SortResult sort(
            CustomDynamicArray<DeliveryRequest> requests,
            RequestSortKey sortKey,
            boolean descending,
            EstimatedDeliveryTimeProvider estimatedDeliveryTimeProvider) {
        RequestComparators.requireRequests(requests);
        RequestComparators.validateEstimatedProvider(sortKey, estimatedDeliveryTimeProvider);
        RequestComparators.validateEstimatedTimes(requests, sortKey, estimatedDeliveryTimeProvider);
        DeliveryRequest[] working = RequestComparators.copyToArray(requests);
        CustomDynamicArray<SortTraceStep> trace = new CustomDynamicArray<>();
        int operations = sortArray(working, sortKey, descending, estimatedDeliveryTimeProvider, trace);
        return new SortResult(toCustomArray(working), operations, trace, false);
    }

    private static int sortArray(
            DeliveryRequest[] working,
            RequestSortKey sortKey,
            boolean descending,
            EstimatedDeliveryTimeProvider provider,
            CustomDynamicArray<SortTraceStep> trace) {
        int operations = 0;
        for (int i = 0; i < working.length - 1; i++) {
            int selectedIndex = i;
            for (int j = i + 1; j < working.length; j++) {
                operations++;
                if (RequestComparators.compare(working[j], working[selectedIndex], sortKey, descending, provider) < 0) {
                    selectedIndex = j;
                }
            }
            if (selectedIndex != i) {
                DeliveryRequest temp = working[i];
                working[i] = working[selectedIndex];
                working[selectedIndex] = temp;
            }
            trace.add(new SortTraceStep("select", i, selectedIndex, snapshot(working)));
        }
        return operations;
    }

    static CustomDynamicArray<DeliveryRequest> toCustomArray(DeliveryRequest[] values) {
        CustomDynamicArray<DeliveryRequest> result = new CustomDynamicArray<>();
        for (DeliveryRequest value : values) {
            result.add(value);
        }
        return result;
    }

    static CustomDynamicArray<String> snapshot(DeliveryRequest[] values) {
        CustomDynamicArray<String> snapshot = new CustomDynamicArray<>();
        for (DeliveryRequest value : values) {
            snapshot.add(value.getRequestId());
        }
        return snapshot;
    }
}
