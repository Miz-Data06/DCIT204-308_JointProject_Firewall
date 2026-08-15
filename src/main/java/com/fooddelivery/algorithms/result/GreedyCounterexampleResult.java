package com.fooddelivery.algorithms.result;

/**
 * Executable evidence that local greedy rider assignment can be globally worse.
 */
public final class GreedyCounterexampleResult {
    private final String firstRequestId;
    private final String greedyFirstRiderId;
    private final String greedySecondRiderId;
    private final double greedyTotalEffectiveTime;
    private final String betterFirstRiderId;
    private final String betterSecondRiderId;
    private final double betterTotalEffectiveTime;

    public GreedyCounterexampleResult(
            String firstRequestId,
            String greedyFirstRiderId,
            String greedySecondRiderId,
            double greedyTotalEffectiveTime,
            String betterFirstRiderId,
            String betterSecondRiderId,
            double betterTotalEffectiveTime) {
        this.firstRequestId = firstRequestId;
        this.greedyFirstRiderId = greedyFirstRiderId;
        this.greedySecondRiderId = greedySecondRiderId;
        this.greedyTotalEffectiveTime = greedyTotalEffectiveTime;
        this.betterFirstRiderId = betterFirstRiderId;
        this.betterSecondRiderId = betterSecondRiderId;
        this.betterTotalEffectiveTime = betterTotalEffectiveTime;
    }

    public String getFirstRequestId() {
        return firstRequestId;
    }

    public String getGreedyFirstRiderId() {
        return greedyFirstRiderId;
    }

    public String getGreedySecondRiderId() {
        return greedySecondRiderId;
    }

    public double getGreedyTotalEffectiveTime() {
        return greedyTotalEffectiveTime;
    }

    public String getBetterFirstRiderId() {
        return betterFirstRiderId;
    }

    public String getBetterSecondRiderId() {
        return betterSecondRiderId;
    }

    public double getBetterTotalEffectiveTime() {
        return betterTotalEffectiveTime;
    }

    public boolean demonstratesGreedyFailure() {
        return betterTotalEffectiveTime < greedyTotalEffectiveTime;
    }
}
