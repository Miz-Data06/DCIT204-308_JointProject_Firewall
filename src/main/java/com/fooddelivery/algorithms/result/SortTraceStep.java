package com.fooddelivery.algorithms.result;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;

/**
 * One independent sorting trace snapshot.
 */
public final class SortTraceStep {
    private final String phase;
    private final int firstIndex;
    private final int secondIndex;
    private final CustomDynamicArray<String> requestIdSnapshot;

    public SortTraceStep(String phase, int firstIndex, int secondIndex, CustomDynamicArray<String> requestIdSnapshot) {
        if (phase == null || phase.isBlank()) {
            throw new IllegalArgumentException("Phase must not be null or blank");
        }
        this.phase = phase;
        this.firstIndex = firstIndex;
        this.secondIndex = secondIndex;
        this.requestIdSnapshot = copySnapshot(requestIdSnapshot);
    }

    public String getPhase() {
        return phase;
    }

    public int getFirstIndex() {
        return firstIndex;
    }

    public int getSecondIndex() {
        return secondIndex;
    }

    public CustomDynamicArray<String> getRequestIdSnapshot() {
        return copySnapshot(requestIdSnapshot);
    }

    private static CustomDynamicArray<String> copySnapshot(CustomDynamicArray<String> snapshot) {
        CustomDynamicArray<String> copy = new CustomDynamicArray<>();
        if (snapshot == null) {
            return copy;
        }
        for (int i = 0; i < snapshot.size(); i++) {
            copy.add(snapshot.get(i));
        }
        return copy;
    }
}
