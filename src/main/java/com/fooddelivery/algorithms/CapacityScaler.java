package com.fooddelivery.algorithms;

import java.math.BigDecimal;

/**
 * Converts double capacities to exact integer DP units using the approved project rule:
 * 1.00 capacity equals 100 integer units.
 */
public final class CapacityScaler {
    private CapacityScaler() {
    }

    /**
     * Converts a finite non-negative capacity with at most two decimal places to integer units.
     *
     * @param capacity original double capacity
     * @return exact capacity multiplied by 100
     * @throws IllegalArgumentException for negative, non-finite, overflowing, or over-precise values
     *
     * Input mutation: none. Time: O(1). Space: O(1).
     * Food-delivery use: makes rider-capacity optimisation exact without rounding parcel capacity.
     */
    public static int scale(double capacity) {
        if (!Double.isFinite(capacity) || capacity < 0.0) {
            throw new IllegalArgumentException("Capacity must be finite and non-negative");
        }
        try {
            return BigDecimal.valueOf(capacity).movePointRight(2).intValueExact();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Capacity must have at most two decimal places and fit integer units", exception);
        }
    }

    public static double unscale(int scaledCapacity) {
        if (scaledCapacity < 0) {
            throw new IllegalArgumentException("Scaled capacity must be non-negative");
        }
        return scaledCapacity / 100.0;
    }
}
