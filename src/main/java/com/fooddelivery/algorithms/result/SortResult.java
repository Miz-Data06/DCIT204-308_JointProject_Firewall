package com.fooddelivery.algorithms.result;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;

/**
 * Immutable sort result with a copied ordering, operation count and trace snapshots.
 */
public final class SortResult {
    private final CustomDynamicArray<DeliveryRequest> sortedRequests;
    private final int operationCount;
    private final CustomDynamicArray<SortTraceStep> trace;
    private final boolean inputModified;

    public SortResult(
            CustomDynamicArray<DeliveryRequest> sortedRequests,
            int operationCount,
            CustomDynamicArray<SortTraceStep> trace,
            boolean inputModified) {
        this.sortedRequests = copyRequests(sortedRequests);
        this.operationCount = operationCount;
        this.trace = copyTrace(trace);
        this.inputModified = inputModified;
    }

    public CustomDynamicArray<DeliveryRequest> getSortedRequests() {
        return copyRequests(sortedRequests);
    }

    public int getOperationCount() {
        return operationCount;
    }

    public CustomDynamicArray<SortTraceStep> getTrace() {
        return copyTrace(trace);
    }

    public boolean isInputModified() {
        return inputModified;
    }

    private static CustomDynamicArray<DeliveryRequest> copyRequests(CustomDynamicArray<DeliveryRequest> requests) {
        CustomDynamicArray<DeliveryRequest> copy = new CustomDynamicArray<>();
        if (requests == null) {
            return copy;
        }
        for (int i = 0; i < requests.size(); i++) {
            copy.add(requests.get(i));
        }
        return copy;
    }

    private static CustomDynamicArray<SortTraceStep> copyTrace(CustomDynamicArray<SortTraceStep> trace) {
        CustomDynamicArray<SortTraceStep> copy = new CustomDynamicArray<>();
        if (trace == null) {
            return copy;
        }
        for (int i = 0; i < trace.size(); i++) {
            copy.add(trace.get(i));
        }
        return copy;
    }
}
