package com.fooddelivery.algorithms;

import com.fooddelivery.algorithms.result.RequestSelectionResult;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;

import java.math.BigDecimal;

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
     * Priority totals use exact BigDecimal.valueOf input values; ties prefer lower capacity,
     * then lexicographically smaller sorted request IDs. Only result/table scores are rounded to double.
     */
    public RequestSelectionResult select(CustomDynamicArray<DeliveryRequest> requests, double riderCapacity) {
        int scaledLimit = CapacityScaler.scale(riderCapacity);
        RequestSelectionSupport.ValidatedRequests validated = RequestSelectionSupport.validateRequests(requests);
        DeliveryRequest[] values = validated.requests();
        int[] scaledCapacities = validated.scaledCapacities();
        BigDecimal[] priorities = validated.priorities();
        long cells;
        try {
            cells = Math.multiplyExact((long) values.length + 1L, (long) scaledLimit + 1L);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("DP table dimensions overflow", exception);
        }
        if (cells > MAX_DP_CELLS) {
            throw new IllegalArgumentException("DP table exceeds safety cell limit: " + MAX_DP_CELLS);
        }

        BigDecimal[][] table = new BigDecimal[values.length + 1][scaledLimit + 1];
        boolean[][] take = new boolean[values.length + 1][scaledLimit + 1];
        int[][] usedCapacities = new int[values.length + 1][scaledLimit + 1];
        String[][][] signatures = new String[values.length + 1][scaledLimit + 1][];
        String[] emptySignature = new String[0];
        for (int capacity = 0; capacity <= scaledLimit; capacity++) {
            table[0][capacity] = BigDecimal.ZERO;
            signatures[0][capacity] = emptySignature;
        }
        for (int item = 1; item <= values.length; item++) {
            int requestIndex = item - 1;
            int requestCapacity = scaledCapacities[requestIndex];
            BigDecimal requestPriority = priorities[requestIndex];
            for (int capacity = 0; capacity <= scaledLimit; capacity++) {
                BigDecimal best = table[item - 1][capacity];
                int bestUsedCapacity = usedCapacities[item - 1][capacity];
                String[] bestSignature = signatures[item - 1][capacity];
                if (requestCapacity <= capacity) {
                    BigDecimal candidate = table[item - 1][capacity - requestCapacity].add(requestPriority);
                    int candidateUsedCapacity = usedCapacities[item - 1][capacity - requestCapacity] + requestCapacity;
                    String[] candidateSignature = RequestSelectionSupport.insertRequestId(
                            signatures[item - 1][capacity - requestCapacity],
                            values[requestIndex].getRequestId());
                    if (RequestSelectionSupport.isBetter(candidate, candidateUsedCapacity, candidateSignature,
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
        BigDecimal bestPriority = BigDecimal.ZERO;
        int bestCapacity = 0;
        String[] bestSignature = emptySignature;
        for (int capacity = 0; capacity <= scaledLimit; capacity++) {
            BigDecimal candidatePriority = table[values.length][capacity];
            int candidateCapacity = usedCapacities[values.length][capacity];
            String[] candidateSignature = signatures[values.length][capacity];
            if (RequestSelectionSupport.isBetter(candidatePriority, candidateCapacity, candidateSignature,
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
                bestPriority.doubleValue(), cells, values.length, scaledLimit, toDoubleTable(table), trace);
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

    private static double[][] toDoubleTable(BigDecimal[][] table) {
        double[][] result = new double[table.length][];
        for (int row = 0; row < table.length; row++) {
            result[row] = new double[table[row].length];
            for (int column = 0; column < table[row].length; column++) {
                result[row][column] = table[row][column].doubleValue();
            }
        }
        return result;
    }
}
