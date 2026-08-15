package com.fooddelivery.algorithms.result;

/**
 * Evidence for one rider considered by greedy assignment.
 */
public final class RiderEvaluation {
    private final String riderId;
    private final boolean eligible;
    private final String rejectionReason;
    private final double effectiveTravelTime;
    private final double distanceKm;

    public RiderEvaluation(
            String riderId,
            boolean eligible,
            String rejectionReason,
            double effectiveTravelTime,
            double distanceKm) {
        this.riderId = riderId;
        this.eligible = eligible;
        this.rejectionReason = rejectionReason;
        this.effectiveTravelTime = effectiveTravelTime;
        this.distanceKm = distanceKm;
    }

    public String getRiderId() {
        return riderId;
    }

    public boolean isEligible() {
        return eligible;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public double getEffectiveTravelTime() {
        return effectiveTravelTime;
    }

    public double getDistanceKm() {
        return distanceKm;
    }
}
