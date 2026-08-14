package com.fooddelivery.algorithms.result;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.Rider;

/**
 * Immutable result for greedy rider assignment.
 */
public final class RiderAssignmentResult {
    private final boolean assigned;
    private final Rider selectedRider;
    private final RouteResult route;
    private final CustomDynamicArray<RiderEvaluation> evaluations;

    public RiderAssignmentResult(
            boolean assigned,
            Rider selectedRider,
            RouteResult route,
            CustomDynamicArray<RiderEvaluation> evaluations) {
        this.assigned = assigned;
        this.selectedRider = selectedRider;
        this.route = route;
        this.evaluations = copyEvaluations(evaluations);
    }

    public boolean isAssigned() {
        return assigned;
    }

    public Rider getSelectedRider() {
        return selectedRider;
    }

    public RouteResult getRoute() {
        return route;
    }

    public CustomDynamicArray<RiderEvaluation> getEvaluations() {
        return copyEvaluations(evaluations);
    }

    private static CustomDynamicArray<RiderEvaluation> copyEvaluations(CustomDynamicArray<RiderEvaluation> evaluations) {
        CustomDynamicArray<RiderEvaluation> copy = new CustomDynamicArray<>();
        if (evaluations == null) {
            return copy;
        }
        for (int i = 0; i < evaluations.size(); i++) {
            copy.add(evaluations.get(i));
        }
        return copy;
    }
}
