package com.fooddelivery.algorithms.result;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;

/**
 * Immutable result for brute-force and dynamic-programming request selection.
 */
public final class RequestSelectionResult {
    private final CustomDynamicArray<DeliveryRequest> selectedRequests;
    private final double totalCapacityUsed;
    private final int totalScaledCapacityUsed;
    private final double totalPriority;
    private final long statesConsidered;
    private final int inputCount;
    private final int scaledCapacityLimit;
    private final double[][] dpTable;
    private final CustomDynamicArray<String> trace;

    public RequestSelectionResult(
            CustomDynamicArray<DeliveryRequest> selectedRequests,
            double totalCapacityUsed,
            int totalScaledCapacityUsed,
            double totalPriority,
            long statesConsidered,
            int inputCount,
            int scaledCapacityLimit,
            double[][] dpTable,
            CustomDynamicArray<String> trace) {
        this.selectedRequests = copyRequests(selectedRequests);
        this.totalCapacityUsed = totalCapacityUsed;
        this.totalScaledCapacityUsed = totalScaledCapacityUsed;
        this.totalPriority = totalPriority;
        this.statesConsidered = statesConsidered;
        this.inputCount = inputCount;
        this.scaledCapacityLimit = scaledCapacityLimit;
        this.dpTable = copyTable(dpTable);
        this.trace = copyTrace(trace);
    }

    public CustomDynamicArray<DeliveryRequest> getSelectedRequests() {
        return copyRequests(selectedRequests);
    }

    public double getTotalCapacityUsed() {
        return totalCapacityUsed;
    }

    public int getTotalScaledCapacityUsed() {
        return totalScaledCapacityUsed;
    }

    public double getTotalPriority() {
        return totalPriority;
    }

    public long getStatesConsidered() {
        return statesConsidered;
    }

    public int getInputCount() {
        return inputCount;
    }

    public int getScaledCapacityLimit() {
        return scaledCapacityLimit;
    }

    public double[][] getDpTable() {
        return copyTable(dpTable);
    }

    public CustomDynamicArray<String> getTrace() {
        return copyTrace(trace);
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

    private static CustomDynamicArray<String> copyTrace(CustomDynamicArray<String> trace) {
        CustomDynamicArray<String> copy = new CustomDynamicArray<>();
        if (trace == null) {
            return copy;
        }
        for (int i = 0; i < trace.size(); i++) {
            copy.add(trace.get(i));
        }
        return copy;
    }

    private static double[][] copyTable(double[][] table) {
        if (table == null) {
            return new double[0][0];
        }
        double[][] copy = new double[table.length][];
        for (int i = 0; i < table.length; i++) {
            copy[i] = new double[table[i].length];
            for (int j = 0; j < table[i].length; j++) {
                copy[i][j] = table[i][j];
            }
        }
        return copy;
    }
}
