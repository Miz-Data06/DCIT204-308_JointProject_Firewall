package com.fooddelivery.algorithms;

import com.fooddelivery.algorithms.result.RequestSelectionResult;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;

import java.math.BigDecimal;

/**
 * Exhaustive request subset selection for small food-delivery batches.
 */
public class BruteForceRequestSelection {
    private static final int MAX_ENUMERATED_REQUESTS = 25;

    /**
     * Selects the highest-priority feasible subset by enumerating every subset.
     *
     * @param requests valid custom request collection
     * @param riderCapacity non-negative finite capacity with at most two decimals
     * @return optimal feasible subset and trace evidence
     * @throws IllegalArgumentException for invalid input or unsafe enumeration size
     *
     * Preconditions: request capacities obey approved scaling. Edge cases: empty requests and zero capacity are valid.
     * Input mutation: none. Time: O(2^n * n). Space: O(n) plus trace.
     * Food-delivery use: exact benchmark for small rider-capacity dispatch decisions.
     * Priority totals use exact BigDecimal.valueOf input values; ties prefer lower capacity,
     * then lexicographically smaller sorted request IDs. Only returned scores are rounded to double.
     */
    public RequestSelectionResult select(CustomDynamicArray<DeliveryRequest> requests, double riderCapacity) {
        int scaledLimit = CapacityScaler.scale(riderCapacity);
        RequestSelectionSupport.ValidatedRequests validated = RequestSelectionSupport.validateRequests(requests);
        DeliveryRequest[] values = validated.requests();
        int[] scaledCapacities = validated.scaledCapacities();
        BigDecimal[] priorities = validated.priorities();
        if (values.length > MAX_ENUMERATED_REQUESTS) {
            throw new IllegalArgumentException("Too many requests for safe brute-force subset enumeration");
        }

        long subsetCount = 1L << values.length;
        BigDecimal bestPriority = BigDecimal.ZERO;
        int bestCapacity = 0;
        CustomDynamicArray<DeliveryRequest> bestSelected = new CustomDynamicArray<>();
        String[] bestSelectedIds = new String[0];
        CustomDynamicArray<String> trace = new CustomDynamicArray<>();

        for (long mask = 0; mask < subsetCount; mask++) {
            long scaledCapacity = 0L;
            BigDecimal priority = BigDecimal.ZERO;
            for (int i = 0; i < values.length; i++) {
                if ((mask & (1L << i)) != 0L) {
                    scaledCapacity += scaledCapacities[i];
                    priority = priority.add(priorities[i]);
                }
            }
            if (scaledCapacity <= scaledLimit) {
                int exactScaledCapacity = (int) scaledCapacity;
                CustomDynamicArray<DeliveryRequest> selected = RequestSelectionSupport.selectedFromMask(values, mask);
                String[] selectedIds = RequestSelectionSupport.requestIds(selected);
                if (RequestSelectionSupport.isBetter(
                        priority, exactScaledCapacity, selectedIds, bestPriority, bestCapacity, bestSelectedIds)) {
                    bestPriority = priority;
                    bestCapacity = exactScaledCapacity;
                    bestSelected = selected;
                    bestSelectedIds = selectedIds;
                    trace.add("accepted mask=" + mask + ", priority=" + priority
                            + ", scaledCapacity=" + exactScaledCapacity);
                }
            }
        }

        return new RequestSelectionResult(bestSelected, CapacityScaler.unscale(bestCapacity), bestCapacity,
                bestPriority.doubleValue(), subsetCount, values.length, scaledLimit, null, trace);
    }
}
