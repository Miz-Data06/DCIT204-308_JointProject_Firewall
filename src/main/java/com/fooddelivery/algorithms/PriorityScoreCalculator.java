package com.fooddelivery.algorithms;

import com.fooddelivery.algorithms.result.PriorityScoreResult;

/**
 * Calculates request priority scores from already-normalized input components.
 */
public class PriorityScoreCalculator {
    /**
     * Calculates a weighted priority score from normalized urgency, deadline-pressure and waiting-time components.
     *
     * @param urgencyComponent finite normalized urgency component in [0.0, 1.0]
     * @param deadlinePressureComponent finite normalized deadline-pressure component in [0.0, 1.0]
     * @param waitingTimeComponent finite normalized waiting-time component in [0.0, 1.0]
     * @return immutable score result with inputs, weights, weighted contributions and final priority
     * @throws IllegalArgumentException for NaN, infinity or values outside [0.0, 1.0]
     *
     * Preconditions: inputs are externally normalized by caller-owned business rules. Edge cases: all-zero and
     * all-one inputs are valid. Input mutation: none. Time: O(1). Space: O(1).
     * Food-delivery use: combine dispatch urgency signals using approved index-derived team weights.
     */
    public PriorityScoreResult calculate(
            double urgencyComponent,
            double deadlinePressureComponent,
            double waitingTimeComponent) {
        double urgency = requireNormalized(urgencyComponent, "Urgency component");
        double deadlinePressure = requireNormalized(deadlinePressureComponent, "Deadline-pressure component");
        double waitingTime = requireNormalized(waitingTimeComponent, "Waiting-time component");

        double urgencyContribution = IndexDerivedPriorityParameters.URGENCY_WEIGHT * urgency;
        double deadlineContribution = IndexDerivedPriorityParameters.DEADLINE_WEIGHT * deadlinePressure;
        double waitingContribution = IndexDerivedPriorityParameters.WAITING_TIME_WEIGHT * waitingTime;
        double priorityScore = urgencyContribution + deadlineContribution + waitingContribution;
        if (!Double.isFinite(priorityScore)
                || priorityScore < -IndexDerivedPriorityParameters.WEIGHT_SUM_TOLERANCE
                || priorityScore > 1.0 + IndexDerivedPriorityParameters.WEIGHT_SUM_TOLERANCE) {
            throw new IllegalArgumentException("Priority score must be finite and normalized");
        }

        return new PriorityScoreResult(
                urgency,
                deadlinePressure,
                waitingTime,
                IndexDerivedPriorityParameters.URGENCY_WEIGHT,
                IndexDerivedPriorityParameters.DEADLINE_WEIGHT,
                IndexDerivedPriorityParameters.WAITING_TIME_WEIGHT,
                urgencyContribution,
                deadlineContribution,
                waitingContribution,
                priorityScore);
    }

    private static double requireNormalized(double value, String fieldName) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(fieldName + " must be finite and in [0.0, 1.0]");
        }
        return value;
    }
}
