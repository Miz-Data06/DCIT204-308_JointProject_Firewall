package com.fooddelivery.algorithms;

/**
 * Approved index-derived parameters for request priority scoring.
 *
 * The project brief requires at least three algorithm parameters derived from team index digits, but it does not
 * prescribe a formula. The team therefore designed this formula by aggregating approved digit positions from
 * anonymized team identifiers into three raw weights: urgency, deadline pressure and waiting time. Personal
 * identifiers are intentionally not stored in source code.
 */
public final class IndexDerivedPriorityParameters {
    public static final int URGENCY_RAW_WEIGHT = 58;
    public static final int DEADLINE_RAW_WEIGHT = 74;
    public static final int WAITING_TIME_RAW_WEIGHT = 72;
    public static final int TOTAL_RAW_WEIGHT = 204;

    public static final double URGENCY_WEIGHT = (double) URGENCY_RAW_WEIGHT / TOTAL_RAW_WEIGHT;
    public static final double DEADLINE_WEIGHT = (double) DEADLINE_RAW_WEIGHT / TOTAL_RAW_WEIGHT;
    public static final double WAITING_TIME_WEIGHT = (double) WAITING_TIME_RAW_WEIGHT / TOTAL_RAW_WEIGHT;
    public static final double WEIGHT_SUM_TOLERANCE = 0.000000000001;

    private IndexDerivedPriorityParameters() {
        throw new AssertionError("Utility class");
    }

    public static double normalizedWeightTotal() {
        return URGENCY_WEIGHT + DEADLINE_WEIGHT + WAITING_TIME_WEIGHT;
    }

    public static boolean weightsSumToOne() {
        return Math.abs(normalizedWeightTotal() - 1.0) <= WEIGHT_SUM_TOLERANCE;
    }
}
