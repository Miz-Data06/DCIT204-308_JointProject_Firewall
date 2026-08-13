package com.fooddelivery.algorithms;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.algorithms.result.SortResult;
import com.fooddelivery.algorithms.result.SortTraceStep;

/**
 * Insertion sort with trace snapshots after each outer insertion.
 */
public class InsertionSort {
    /**
     * Sorts a copy using insertion sort.
     *
     * @param requests valid request array
     * @param sortKey selected request field
     * @return copied sorted result with trace and operation count
     * @throws IllegalArgumentException for invalid input or missing estimated-time provider
     *
     * Preconditions: none beyond valid inputs. Edge cases: empty and one-item inputs succeed.
     * Input mutation: none. Time: best O(n), average O(n^2), worst O(n^2). Space: O(n) copied result plus trace.
     * Food-delivery use: efficient demonstration for nearly sorted request lists.
     */
    public SortResult sort(CustomDynamicArray<DeliveryRequest> requests, RequestSortKey sortKey) {
        return sort(requests, sortKey, false, null);
    }

    /**
     * Sorts a copy, optionally in descending primary-key order.
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
        return new SortResult(SelectionSort.toCustomArray(working), operations, trace, false);
    }

    private static int sortArray(
            DeliveryRequest[] working,
            RequestSortKey sortKey,
            boolean descending,
            EstimatedDeliveryTimeProvider provider,
            CustomDynamicArray<SortTraceStep> trace) {
        int operations = 0;
        for (int i = 1; i < working.length; i++) {
            DeliveryRequest current = working[i];
            int j = i - 1;
            while (j >= 0) {
                operations++;
                if (RequestComparators.compare(working[j], current, sortKey, descending, provider) <= 0) {
                    break;
                }
                working[j + 1] = working[j];
                j--;
            }
            working[j + 1] = current;
            trace.add(new SortTraceStep("insert", i, j + 1, SelectionSort.snapshot(working)));
        }
        return operations;
    }
}
