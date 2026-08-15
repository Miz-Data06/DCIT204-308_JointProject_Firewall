package com.fooddelivery.algorithms.result;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;

/**
 * Immutable BFS/DFS result containing traversal order and trace evidence.
 */
public final class TraversalResult {
    private final CustomDynamicArray<String> order;
    private final CustomDynamicArray<TraversalStep> trace;

    public TraversalResult(CustomDynamicArray<String> order, CustomDynamicArray<TraversalStep> trace) {
        this.order = copyStrings(order);
        this.trace = copyTrace(trace);
    }

    public CustomDynamicArray<String> getOrder() {
        return copyStrings(order);
    }

    public CustomDynamicArray<TraversalStep> getTrace() {
        return copyTrace(trace);
    }

    private static CustomDynamicArray<String> copyStrings(CustomDynamicArray<String> values) {
        CustomDynamicArray<String> copy = new CustomDynamicArray<>();
        if (values == null) {
            return copy;
        }
        for (int i = 0; i < values.size(); i++) {
            copy.add(values.get(i));
        }
        return copy;
    }

    private static CustomDynamicArray<TraversalStep> copyTrace(CustomDynamicArray<TraversalStep> values) {
        CustomDynamicArray<TraversalStep> copy = new CustomDynamicArray<>();
        if (values == null) {
            return copy;
        }
        for (int i = 0; i < values.size(); i++) {
            copy.add(values.get(i));
        }
        return copy;
    }
}
