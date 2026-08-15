package com.fooddelivery.algorithms.result;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.Road;

/**
 * Immutable minimum spanning tree or forest result.
 */
public final class MstResult {
    private final CustomDynamicArray<Road> selectedRoads;
    private final double totalEffectiveCost;
    private final int vertexCount;
    private final int componentCount;
    private final CustomDynamicArray<MstStep> trace;

    public MstResult(
            CustomDynamicArray<Road> selectedRoads,
            double totalEffectiveCost,
            int vertexCount,
            int componentCount,
            CustomDynamicArray<MstStep> trace) {
        this.selectedRoads = copyRoads(selectedRoads);
        this.totalEffectiveCost = totalEffectiveCost;
        this.vertexCount = vertexCount;
        this.componentCount = componentCount;
        this.trace = copyTrace(trace);
    }

    public CustomDynamicArray<Road> getSelectedRoads() {
        return copyRoads(selectedRoads);
    }

    public double getTotalEffectiveCost() {
        return totalEffectiveCost;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getComponentCount() {
        return componentCount;
    }

    public boolean isConnectedTree() {
        return vertexCount > 0 && componentCount == 1;
    }

    public boolean isForest() {
        return componentCount != 1;
    }

    public CustomDynamicArray<MstStep> getTrace() {
        return copyTrace(trace);
    }

    private static CustomDynamicArray<Road> copyRoads(CustomDynamicArray<Road> roads) {
        CustomDynamicArray<Road> copy = new CustomDynamicArray<>();
        if (roads == null) {
            return copy;
        }
        for (int i = 0; i < roads.size(); i++) {
            copy.add(roads.get(i));
        }
        return copy;
    }

    private static CustomDynamicArray<MstStep> copyTrace(CustomDynamicArray<MstStep> trace) {
        CustomDynamicArray<MstStep> copy = new CustomDynamicArray<>();
        if (trace == null) {
            return copy;
        }
        for (int i = 0; i < trace.size(); i++) {
            copy.add(trace.get(i));
        }
        return copy;
    }
}
