package com.fooddelivery.algorithms.result;

/**
 * Immutable evidence for an index-derived priority score calculation.
 */
public final class PriorityScoreResult {
    private final double urgencyComponent;
    private final double deadlinePressureComponent;
    private final double waitingTimeComponent;
    private final double urgencyWeight;
    private final double deadlineWeight;
    private final double waitingTimeWeight;
    private final double urgencyContribution;
    private final double deadlineContribution;
    private final double waitingTimeContribution;
    private final double priorityScore;

    public PriorityScoreResult(
            double urgencyComponent,
            double deadlinePressureComponent,
            double waitingTimeComponent,
            double urgencyWeight,
            double deadlineWeight,
            double waitingTimeWeight,
            double urgencyContribution,
            double deadlineContribution,
            double waitingTimeContribution,
            double priorityScore) {
        this.urgencyComponent = urgencyComponent;
        this.deadlinePressureComponent = deadlinePressureComponent;
        this.waitingTimeComponent = waitingTimeComponent;
        this.urgencyWeight = urgencyWeight;
        this.deadlineWeight = deadlineWeight;
        this.waitingTimeWeight = waitingTimeWeight;
        this.urgencyContribution = urgencyContribution;
        this.deadlineContribution = deadlineContribution;
        this.waitingTimeContribution = waitingTimeContribution;
        this.priorityScore = priorityScore;
    }

    public double getUrgencyComponent() {
        return urgencyComponent;
    }

    public double getDeadlinePressureComponent() {
        return deadlinePressureComponent;
    }

    public double getWaitingTimeComponent() {
        return waitingTimeComponent;
    }

    public double getUrgencyWeight() {
        return urgencyWeight;
    }

    public double getDeadlineWeight() {
        return deadlineWeight;
    }

    public double getWaitingTimeWeight() {
        return waitingTimeWeight;
    }

    public double getUrgencyContribution() {
        return urgencyContribution;
    }

    public double getDeadlineContribution() {
        return deadlineContribution;
    }

    public double getWaitingTimeContribution() {
        return waitingTimeContribution;
    }

    public double getPriorityScore() {
        return priorityScore;
    }
}
