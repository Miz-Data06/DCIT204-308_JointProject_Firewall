package com.fooddelivery.algorithms;

import com.fooddelivery.model.Road;

final class MstEdgeOrdering {
    private MstEdgeOrdering() {
    }

    static int compare(Road first, Road second) {
        GraphAlgorithmSupport.validateRoadWeight(first);
        GraphAlgorithmSupport.validateRoadWeight(second);
        int comparison = Double.compare(first.getEffectiveTime(), second.getEffectiveTime());
        if (comparison != 0) {
            return comparison;
        }
        comparison = normalizedFrom(first).compareTo(normalizedFrom(second));
        if (comparison != 0) {
            return comparison;
        }
        comparison = normalizedTo(first).compareTo(normalizedTo(second));
        if (comparison != 0) {
            return comparison;
        }
        return first.getRoadId().compareTo(second.getRoadId());
    }

    static String normalizedFrom(Road road) {
        return road.getFromLocationId().compareTo(road.getToLocationId()) <= 0
                ? road.getFromLocationId()
                : road.getToLocationId();
    }

    static String normalizedTo(Road road) {
        return road.getFromLocationId().compareTo(road.getToLocationId()) <= 0
                ? road.getToLocationId()
                : road.getFromLocationId();
    }

    static void sort(Road[] roads) {
        for (int i = 1; i < roads.length; i++) {
            Road current = roads[i];
            int j = i - 1;
            while (j >= 0 && compare(roads[j], current) > 0) {
                roads[j + 1] = roads[j];
                j--;
            }
            roads[j + 1] = current;
        }
    }
}
