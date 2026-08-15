package com.fooddelivery.algorithms;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.algorithms.result.SortResult;
import com.fooddelivery.algorithms.result.SortTraceStep;

/**
 * Quicksort with a deterministic last-element pivot rule.
 */
public class QuickSort {
    /**
     * Sorts a copy using deterministic quicksort.
     *
     * @param requests valid request array
     * @param sortKey selected request field
     * @return copied sorted result with partition trace and operation count
     * @throws IllegalArgumentException for invalid input or missing estimated-time provider
     *
     * Preconditions: none beyond valid inputs. Edge cases: empty and one-item inputs succeed.
     * Input mutation: none. Time: best O(n log n), average O(n log n), worst O(n^2).
     * Space: O(n) copied result plus O(log n) average recursion stack and trace.
     * Food-delivery use: fast general-purpose request ordering demonstration.
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
        int operations = quickSort(working, 0, working.length - 1, sortKey, descending, estimatedDeliveryTimeProvider, trace);
        return new SortResult(SelectionSort.toCustomArray(working), operations, trace, false);
    }

    private static int quickSort(
            DeliveryRequest[] working,
            int low,
            int high,
            RequestSortKey sortKey,
            boolean descending,
            EstimatedDeliveryTimeProvider provider,
            CustomDynamicArray<SortTraceStep> trace) {
        if (low >= high) {
            return 0;
        }
        PartitionResult partition = partition(working, low, high, sortKey, descending, provider);
        trace.add(new SortTraceStep("partition", low, high, SelectionSort.snapshot(working)));
        int operations = partition.operationCount;
        operations += quickSort(working, low, partition.pivotIndex - 1, sortKey, descending, provider, trace);
        operations += quickSort(working, partition.pivotIndex + 1, high, sortKey, descending, provider, trace);
        return operations;
    }

    private static PartitionResult partition(
            DeliveryRequest[] working,
            int low,
            int high,
            RequestSortKey sortKey,
            boolean descending,
            EstimatedDeliveryTimeProvider provider) {
        DeliveryRequest pivot = working[high];
        int boundary = low - 1;
        int operations = 0;
        for (int current = low; current < high; current++) {
            operations++;
            if (RequestComparators.compare(working[current], pivot, sortKey, descending, provider) <= 0) {
                boundary++;
                swap(working, boundary, current);
            }
        }
        swap(working, boundary + 1, high);
        return new PartitionResult(boundary + 1, operations);
    }

    private static void swap(DeliveryRequest[] working, int firstIndex, int secondIndex) {
        if (firstIndex == secondIndex) {
            return;
        }
        DeliveryRequest temp = working[firstIndex];
        working[firstIndex] = working[secondIndex];
        working[secondIndex] = temp;
    }

    private static final class PartitionResult {
        private final int pivotIndex;
        private final int operationCount;

        private PartitionResult(int pivotIndex, int operationCount) {
            this.pivotIndex = pivotIndex;
            this.operationCount = operationCount;
        }
    }
}
