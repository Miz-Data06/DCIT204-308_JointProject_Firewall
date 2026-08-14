package com.fooddelivery.algorithms;

import com.fooddelivery.algorithms.result.RequestSelectionResult;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;

/**
 * 0/1 knapsack tabulation for request selection using exact capacity scaling.
 */
public class DynamicProgrammingKnapsack {
    private static final long MAX_DP_CELLS = 2_000_000L;

    /**
     * Selects requests by maximising priority without exceeding rider capacity.
     *
     * @param requests valid custom request collection
     * @param riderCapacity non-negative finite capacity with at most two decimals
     * @return optimal selected requests, copied DP table and reconstruction trace
     * @throws IllegalArgumentException for invalid values or table sizes beyond the safety limit
     *
     * Preconditions: capacities are exactly convertible to integer units. Edge cases: empty and zero-capacity inputs
     * are valid. Input mutation: none. Time: O(nC). Space: O(nC), capped by MAX_DP_CELLS.
     * Food-delivery use: exact scalable capacity-constrained prioritisation for riders.
     */
    public RequestSelectionResult select(CustomDynamicArray<DeliveryRequest> requests, double riderCapacity) {
        int scaledLimit = CapacityScaler.scale(riderCapacity);
        RequestSelectionSupport.ValidatedRequests validated = RequestSelectionSupport.validateRequests(requests);
        DeliveryRequest[] values = validated.requests();
        int[] scaledCapacities = validated.scaledCapacities();
        long cells;
        try {
            cells = Math.multiplyExact((long) values.length + 1L, (long) scaledLimit + 1L);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("DP table dimensions overflow", exception);
        }
        if (cells > MAX_DP_CELLS) {
            throw new IllegalArgumentException("DP table exceeds safety cell limit: " + MAX_DP_CELLS);
        }

        double[][] table = new double[values.length + 1][scaledLimit + 1];
        boolean[][] take = new boolean[values.length + 1][scaledLimit + 1];
        int[][] usedCapacities = new int[values.length + 1][scaledLimit + 1];
        String[][] signatures = new String[values.length + 1][scaledLimit + 1];
        for (int capacity = 0; capacity <= scaledLimit; capacity++) {
            signatures[0][capacity] = "";
        }
        for (int item = 1; item <= values.length; item++) {
            int requestIndex = item - 1;
            int requestCapacity = scaledCapacities[requestIndex];
            double requestPriority = values[requestIndex].getPriorityScore();
            for (int capacity = 0; capacity <= scaledLimit; capacity++) {
                double best = table[item - 1][capacity];
                int bestUsedCapacity = usedCapacities[item - 1][capacity];
                String bestSignature = signatures[item - 1][capacity];
                if (requestCapacity <= capacity) {
                    double candidate = table[item - 1][capacity - requestCapacity] + requestPriority;
                    int candidateUsedCapacity = usedCapacities[item - 1][capacity - requestCapacity] + requestCapacity;
                    String candidateSignature = addRequestId(
                            signatures[item - 1][capacity - requestCapacity],
                            values[requestIndex].getRequestId());
                    if (isBetterCell(candidate, candidateUsedCapacity, candidateSignature,
                            best, bestUsedCapacity, bestSignature)) {
                        best = candidate;
                        bestUsedCapacity = candidateUsedCapacity;
                        bestSignature = candidateSignature;
                        take[item][capacity] = true;
                    }
                }
                table[item][capacity] = best;
                usedCapacities[item][capacity] = bestUsedCapacity;
                signatures[item][capacity] = bestSignature;
            }
        }

        CustomDynamicArray<DeliveryRequest> bestSelected = new CustomDynamicArray<>();
        double bestPriority = -1.0;
        int bestCapacity = 0;
        String bestSignature = "";
        for (int capacity = 0; capacity <= scaledLimit; capacity++) {
            double candidatePriority = table[values.length][capacity];
            int candidateCapacity = usedCapacities[values.length][capacity];
            String candidateSignature = signatures[values.length][capacity];
            if (isBetterCell(candidatePriority, candidateCapacity, candidateSignature,
                    bestPriority, bestCapacity, bestSignature)) {
                bestPriority = candidatePriority;
                bestCapacity = candidateCapacity;
                bestSignature = candidateSignature;
                bestSelected = reconstruct(values, scaledCapacities, take, values.length, capacity);
            }
        }

        CustomDynamicArray<String> trace = new CustomDynamicArray<>();
        trace.add("dpRows=" + (values.length + 1) + ", dpColumns=" + (scaledLimit + 1));
        trace.add("selectedScaledCapacity=" + bestCapacity);
        return new RequestSelectionResult(bestSelected, CapacityScaler.unscale(bestCapacity), bestCapacity,
                Math.max(0.0, bestPriority), cells, values.length, scaledLimit, table, trace);
    }

    private static CustomDynamicArray<DeliveryRequest> reconstruct(
            DeliveryRequest[] values,
            int[] scaledCapacities,
            boolean[][] take,
            int item,
            int capacity) {
        CustomDynamicArray<DeliveryRequest> selected = new CustomDynamicArray<>();
        int remainingCapacity = capacity;
        for (int currentItem = item; currentItem >= 1; currentItem--) {
            if (take[currentItem][remainingCapacity]) {
                int requestIndex = currentItem - 1;
                selected.add(values[requestIndex]);
                remainingCapacity -= scaledCapacities[requestIndex];
            }
        }
        RequestSelectionSupport.sortByRequestId(selected);
        return selected;
    }

    private static boolean isBetterCell(
            double priority,
            int scaledCapacity,
            String signature,
            double bestPriority,
            int bestScaledCapacity,
            String bestSignature) {
        int priorityComparison = Double.compare(priority, bestPriority);
        if (priorityComparison > 0) {
            return true;
        }
        if (priorityComparison < 0) {
            return false;
        }
        if (scaledCapacity < bestScaledCapacity) {
            return true;
        }
        if (scaledCapacity > bestScaledCapacity) {
            return false;
        }
        return compareSignatures(signature, bestSignature) < 0;
    }

    private static String addRequestId(String signature, String requestId) {
        if (signature == null || signature.isEmpty()) {
            return requestId;
        }
        String[] ids = signature.split(",");
        StringBuilder builder = new StringBuilder();
        boolean added = false;
        for (String id : ids) {
            if (!added && requestId.compareTo(id) < 0) {
                appendId(builder, requestId);
                added = true;
            }
            appendId(builder, id);
        }
        if (!added) {
            appendId(builder, requestId);
        }
        return builder.toString();
    }

    private static int compareSignatures(String first, String second) {
        String normalFirst = first == null ? "" : first;
        String normalSecond = second == null ? "" : second;
        if (normalFirst.isEmpty() || normalSecond.isEmpty()) {
            return Integer.compare(normalFirst.length(), normalSecond.length());
        }
        String[] firstIds = normalFirst.split(",");
        String[] secondIds = normalSecond.split(",");
        int minimum = Math.min(firstIds.length, secondIds.length);
        for (int i = 0; i < minimum; i++) {
            int comparison = firstIds[i].compareTo(secondIds[i]);
            if (comparison != 0) {
                return comparison;
            }
        }
        return Integer.compare(firstIds.length, secondIds.length);
    }

    private static void appendId(StringBuilder builder, String requestId) {
        if (builder.length() > 0) {
            builder.append(',');
        }
        builder.append(requestId);
    }
}
