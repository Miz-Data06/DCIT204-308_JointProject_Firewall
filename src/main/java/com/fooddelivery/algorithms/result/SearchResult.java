package com.fooddelivery.algorithms.result;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;

/**
 * Immutable result for request search algorithms.
 */
public final class SearchResult {
    private static final int NOT_FOUND_INDEX = -1;

    private final boolean found;
    private final int index;
    private final DeliveryRequest request;
    private final int operationCount;
    private final CustomDynamicArray<BinarySearchStep> binarySearchTrace;
    private final CustomDynamicArray<String> linearSearchTrace;

    private SearchResult(
            boolean found,
            int index,
            DeliveryRequest request,
            int operationCount,
            CustomDynamicArray<BinarySearchStep> binarySearchTrace,
            CustomDynamicArray<String> linearSearchTrace) {
        this.found = found;
        this.index = index;
        this.request = request;
        this.operationCount = operationCount;
        this.binarySearchTrace = copyBinaryTrace(binarySearchTrace);
        this.linearSearchTrace = copyLinearTrace(linearSearchTrace);
    }

    public static SearchResult linear(
            boolean found,
            int index,
            DeliveryRequest request,
            int operationCount,
            CustomDynamicArray<String> trace) {
        return new SearchResult(found, found ? index : NOT_FOUND_INDEX, request, operationCount, null, trace);
    }

    public static SearchResult binary(
            boolean found,
            int index,
            DeliveryRequest request,
            int operationCount,
            CustomDynamicArray<BinarySearchStep> trace) {
        return new SearchResult(found, found ? index : NOT_FOUND_INDEX, request, operationCount, trace, null);
    }

    public boolean isFound() {
        return found;
    }

    public int getIndex() {
        return index;
    }

    public DeliveryRequest getRequest() {
        return request;
    }

    public int getOperationCount() {
        return operationCount;
    }

    public CustomDynamicArray<BinarySearchStep> getBinarySearchTrace() {
        return copyBinaryTrace(binarySearchTrace);
    }

    public CustomDynamicArray<String> getLinearSearchTrace() {
        return copyLinearTrace(linearSearchTrace);
    }

    private static CustomDynamicArray<BinarySearchStep> copyBinaryTrace(CustomDynamicArray<BinarySearchStep> trace) {
        CustomDynamicArray<BinarySearchStep> copy = new CustomDynamicArray<>();
        if (trace == null) {
            return copy;
        }
        for (int i = 0; i < trace.size(); i++) {
            copy.add(trace.get(i));
        }
        return copy;
    }

    private static CustomDynamicArray<String> copyLinearTrace(CustomDynamicArray<String> trace) {
        CustomDynamicArray<String> copy = new CustomDynamicArray<>();
        if (trace == null) {
            return copy;
        }
        for (int i = 0; i < trace.size(); i++) {
            copy.add(trace.get(i));
        }
        return copy;
    }
}
