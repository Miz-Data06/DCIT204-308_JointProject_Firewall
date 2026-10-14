package com.fooddelivery.algorithms;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;

import java.math.BigDecimal;

final class RequestSelectionSupport {
    private RequestSelectionSupport() {
    }

    static ValidatedRequests validateRequests(CustomDynamicArray<DeliveryRequest> requests) {
        if (requests == null) {
            throw new IllegalArgumentException("Requests must not be null");
        }
        DeliveryRequest[] values = new DeliveryRequest[requests.size()];
        int[] scaledCapacities = new int[requests.size()];
        for (int i = 0; i < requests.size(); i++) {
            DeliveryRequest request = requests.get(i);
            if (request == null) {
                throw new IllegalArgumentException("Request entries must not be null");
            }
            if (!Double.isFinite(request.getPriorityScore()) || request.getPriorityScore() < 0.0) {
                throw new IllegalArgumentException("Request priority must be finite and non-negative");
            }
            values[i] = request;
            scaledCapacities[i] = CapacityScaler.scale(request.getCapacityRequired());
        }
        BigDecimal[] priorities = new BigDecimal[values.length];
        for (int i = 0; i < values.length; i++) {
            priorities[i] = BigDecimal.valueOf(values[i].getPriorityScore());
        }
        return new ValidatedRequests(values, scaledCapacities, priorities);
    }

    static CustomDynamicArray<DeliveryRequest> selectedFromMask(DeliveryRequest[] requests, long mask) {
        CustomDynamicArray<DeliveryRequest> selected = new CustomDynamicArray<>();
        for (int i = 0; i < requests.length; i++) {
            if ((mask & (1L << i)) != 0L) {
                selected.add(requests[i]);
            }
        }
        sortByRequestId(selected);
        return selected;
    }

    static void sortByRequestId(CustomDynamicArray<DeliveryRequest> selected) {
        for (int i = 1; i < selected.size(); i++) {
            DeliveryRequest current = selected.get(i);
            int j = i - 1;
            while (j >= 0 && selected.get(j).getRequestId().compareTo(current.getRequestId()) > 0) {
                selected.set(j + 1, selected.get(j));
                j--;
            }
            selected.set(j + 1, current);
        }
    }

    /**
     * Canonical exact ordering: larger priority total, then lower scaled capacity,
     * then lexicographically smaller sorted request-ID sequence.
     */
    static boolean isBetter(
            BigDecimal priority,
            int scaledCapacity,
            String[] selectedIds,
            BigDecimal bestPriority,
            int bestScaledCapacity,
            String[] bestSelectedIds) {
        int priorityComparison = priority.compareTo(bestPriority);
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
        return compareRequestIdSequences(selectedIds, bestSelectedIds) < 0;
    }

    static String[] requestIds(CustomDynamicArray<DeliveryRequest> selected) {
        String[] ids = new String[selected.size()];
        for (int i = 0; i < selected.size(); i++) {
            ids[i] = selected.get(i).getRequestId();
        }
        return ids;
    }

    static String[] insertRequestId(String[] ids, String requestId) {
        String[] result = new String[ids.length + 1];
        int insertionPoint = 0;
        while (insertionPoint < ids.length && ids[insertionPoint].compareTo(requestId) <= 0) {
            insertionPoint++;
        }
        System.arraycopy(ids, 0, result, 0, insertionPoint);
        result[insertionPoint] = requestId;
        System.arraycopy(ids, insertionPoint, result, insertionPoint + 1, ids.length - insertionPoint);
        return result;
    }

    static double totalPriority(CustomDynamicArray<DeliveryRequest> selected) {
        double total = 0.0;
        for (int i = 0; i < selected.size(); i++) {
            total += selected.get(i).getPriorityScore();
        }
        return total;
    }

    static int totalScaledCapacity(CustomDynamicArray<DeliveryRequest> selected) {
        long total = 0L;
        for (int i = 0; i < selected.size(); i++) {
            total += CapacityScaler.scale(selected.get(i).getCapacityRequired());
        }
        try {
            return Math.toIntExact(total);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Selected request capacity total overflows integer scaling", exception);
        }
    }

    private static int compareRequestIdSequences(
            String[] first,
            String[] second) {
        int minimum = Math.min(first.length, second.length);
        for (int i = 0; i < minimum; i++) {
            int comparison = first[i].compareTo(second[i]);
            if (comparison != 0) {
                return comparison;
            }
        }
        return Integer.compare(first.length, second.length);
    }

    static final class ValidatedRequests {
        private final DeliveryRequest[] requests;
        private final int[] scaledCapacities;
        private final BigDecimal[] priorities;

        private ValidatedRequests(DeliveryRequest[] requests, int[] scaledCapacities, BigDecimal[] priorities) {
            this.requests = requests;
            this.scaledCapacities = scaledCapacities;
            this.priorities = priorities;
        }

        DeliveryRequest[] requests() {
            return requests;
        }

        int[] scaledCapacities() {
            return scaledCapacities;
        }

        BigDecimal[] priorities() {
            return priorities;
        }
    }
}
