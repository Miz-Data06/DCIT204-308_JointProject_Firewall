package com.fooddelivery.algorithms;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;

final class RequestSelectionSupport {
    private static final double EPSILON = 0.000000001;

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
        return new ValidatedRequests(values, scaledCapacities);
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

    static boolean isBetter(
            double priority,
            int scaledCapacity,
            CustomDynamicArray<DeliveryRequest> selected,
            double bestPriority,
            int bestScaledCapacity,
            CustomDynamicArray<DeliveryRequest> bestSelected) {
        int priorityComparison = compareDouble(priority, bestPriority);
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
        return compareRequestIdSequences(selected, bestSelected) < 0;
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

    private static int compareDouble(double first, double second) {
        if (Math.abs(first - second) <= EPSILON) {
            return 0;
        }
        return first < second ? -1 : 1;
    }

    private static int compareRequestIdSequences(
            CustomDynamicArray<DeliveryRequest> first,
            CustomDynamicArray<DeliveryRequest> second) {
        int minimum = Math.min(first.size(), second.size());
        for (int i = 0; i < minimum; i++) {
            int comparison = first.get(i).getRequestId().compareTo(second.get(i).getRequestId());
            if (comparison != 0) {
                return comparison;
            }
        }
        return Integer.compare(first.size(), second.size());
    }

    static final class ValidatedRequests {
        private final DeliveryRequest[] requests;
        private final int[] scaledCapacities;

        private ValidatedRequests(DeliveryRequest[] requests, int[] scaledCapacities) {
            this.requests = requests;
            this.scaledCapacities = scaledCapacities;
        }

        DeliveryRequest[] requests() {
            return requests;
        }

        int[] scaledCapacities() {
            return scaledCapacities;
        }
    }
}
