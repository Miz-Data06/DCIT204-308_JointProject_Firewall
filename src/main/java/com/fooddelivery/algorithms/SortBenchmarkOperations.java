package com.fooddelivery.algorithms;

import com.fooddelivery.algorithms.result.SortResult;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;

/** Benchmark-only facade for the existing sorting implementations without trace allocation. */
public final class SortBenchmarkOperations {
    private SortBenchmarkOperations() {
    }

    public static SortResult sortWithoutTrace(
            String algorithm,
            CustomDynamicArray<DeliveryRequest> requests,
            RequestSortKey sortKey) {
        return switch (algorithm) {
            case "selection_sort" -> new SelectionSort().sortWithoutTrace(requests, sortKey, false, null);
            case "insertion_sort" -> new InsertionSort().sortWithoutTrace(requests, sortKey, false, null);
            case "merge_sort" -> new MergeSort().sortWithoutTrace(requests, sortKey, false, null);
            case "quick_sort" -> new QuickSort().sortWithoutTrace(requests, sortKey, false, null);
            default -> throw new IllegalArgumentException("Unknown benchmark sort: " + algorithm);
        };
    }
}
