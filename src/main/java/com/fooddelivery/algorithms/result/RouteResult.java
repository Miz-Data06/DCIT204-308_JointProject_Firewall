package com.fooddelivery.algorithms.result;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;

/**
 * Immutable fastest-route result with path, totals and Dijkstra trace evidence.
 */
public final class RouteResult {
    private final boolean reachable;
    private final CustomDynamicArray<String> path;
    private final CustomDynamicArray<Double> edgeEffectiveTimes;
    private final double totalDistanceKm;
    private final double totalEffectiveTime;
    private final CustomDynamicArray<DijkstraStep> trace;

    public RouteResult(
            boolean reachable,
            CustomDynamicArray<String> path,
            double totalDistanceKm,
            double totalEffectiveTime,
            CustomDynamicArray<DijkstraStep> trace) {
        this(reachable, path, new CustomDynamicArray<>(), totalDistanceKm, totalEffectiveTime, trace);
    }

    public RouteResult(
            boolean reachable,
            CustomDynamicArray<String> path,
            CustomDynamicArray<Double> edgeEffectiveTimes,
            double totalDistanceKm,
            double totalEffectiveTime,
            CustomDynamicArray<DijkstraStep> trace) {
        this.reachable = reachable;
        this.path = copyStrings(path);
        this.edgeEffectiveTimes = copyDoubles(edgeEffectiveTimes);
        this.totalDistanceKm = totalDistanceKm;
        this.totalEffectiveTime = totalEffectiveTime;
        this.trace = copyTrace(trace);
    }

    public boolean isReachable() {
        return reachable;
    }

    public CustomDynamicArray<String> getPath() {
        return copyStrings(path);
    }

    public CustomDynamicArray<Double> getEdgeEffectiveTimes() {
        return copyDoubles(edgeEffectiveTimes);
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public double getTotalEffectiveTime() {
        return totalEffectiveTime;
    }

    public CustomDynamicArray<DijkstraStep> getTrace() {
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

    private static CustomDynamicArray<Double> copyDoubles(CustomDynamicArray<Double> values) {
        CustomDynamicArray<Double> copy = new CustomDynamicArray<>();
        if (values == null) {
            return copy;
        }
        for (int i = 0; i < values.size(); i++) {
            copy.add(values.get(i));
        }
        return copy;
    }

    private static CustomDynamicArray<DijkstraStep> copyTrace(CustomDynamicArray<DijkstraStep> values) {
        CustomDynamicArray<DijkstraStep> copy = new CustomDynamicArray<>();
        if (values == null) {
            return copy;
        }
        for (int i = 0; i < values.size(); i++) {
            copy.add(values.get(i));
        }
        return copy;
    }
}
