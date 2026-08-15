package com.fooddelivery.algorithms;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.algorithms.result.SortResult;
import com.fooddelivery.algorithms.result.SortTraceStep;

/**
 * Merge sort for deterministic request ordering.
 */
public class MergeSort {
    /**
     * Sorts a copy using merge sort.
     *
     * @param requests valid request array
     * @param sortKey selected request field
     * @return copied sorted result with merge trace and operation count
     * @throws IllegalArgumentException for invalid input or missing estimated-time provider
     *
     * Preconditions: none beyond valid inputs. Edge cases: empty and one-item inputs succeed.
     * Input mutation: none. Time: best/average/worst O(n log n). Space: O(n) temporary array plus trace.
     * Food-delivery use: predictable sorting for larger request batches.
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
        DeliveryRequest[] temporary = new DeliveryRequest[working.length];
        CustomDynamicArray<SortTraceStep> trace = new CustomDynamicArray<>();
        int operations = mergeSort(working, temporary, 0, working.length - 1, sortKey, descending,
                estimatedDeliveryTimeProvider, trace);
        return new SortResult(SelectionSort.toCustomArray(working), operations, trace, false);
    }

    private static int mergeSort(
            DeliveryRequest[] working,
            DeliveryRequest[] temporary,
            int left,
            int right,
            RequestSortKey sortKey,
            boolean descending,
            EstimatedDeliveryTimeProvider provider,
            CustomDynamicArray<SortTraceStep> trace) {
        if (left >= right) {
            return 0;
        }
        int middle = left + (right - left) / 2;
        int operations = mergeSort(working, temporary, left, middle, sortKey, descending, provider, trace);
        operations += mergeSort(working, temporary, middle + 1, right, sortKey, descending, provider, trace);
        operations += merge(working, temporary, left, middle, right, sortKey, descending, provider);
        trace.add(new SortTraceStep("merge", left, right, SelectionSort.snapshot(working)));
        return operations;
    }

    private static int merge(
            DeliveryRequest[] working,
            DeliveryRequest[] temporary,
            int left,
            int middle,
            int right,
            RequestSortKey sortKey,
            boolean descending,
            EstimatedDeliveryTimeProvider provider) {
        for (int i = left; i <= right; i++) {
            temporary[i] = working[i];
        }

        int leftIndex = left;
        int rightIndex = middle + 1;
        int targetIndex = left;
        int operations = 0;

        while (leftIndex <= middle && rightIndex <= right) {
            operations++;
            if (RequestComparators.compare(temporary[leftIndex], temporary[rightIndex], sortKey, descending, provider) <= 0) {
                working[targetIndex] = temporary[leftIndex];
                leftIndex++;
            } else {
                working[targetIndex] = temporary[rightIndex];
                rightIndex++;
            }
            targetIndex++;
        }

        while (leftIndex <= middle) {
            working[targetIndex] = temporary[leftIndex];
            leftIndex++;
            targetIndex++;
        }
        while (rightIndex <= right) {
            working[targetIndex] = temporary[rightIndex];
            rightIndex++;
            targetIndex++;
        }
        return operations;
    }
}
