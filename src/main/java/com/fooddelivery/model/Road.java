package com.fooddelivery.model;

import java.util.Objects;

/**
 * Immutable shared road model for weighted graph construction and routing.
 */
public class Road {
    private final String roadId;
    private final String fromLocationId;
    private final String toLocationId;
    private final double distanceKm;
    private final double normalTravelTimeMinutes;
    private final double roadConditionWeight;

    /**
     * Creates a validated road edge with fixed endpoints and positive weights.
     */
    public Road(
            String roadId,
            String fromLocationId,
            String toLocationId,
            double distanceKm,
            double normalTravelTimeMinutes,
            double roadConditionWeight) {
        this.roadId = requireNonBlank(roadId, "Road ID");
        this.fromLocationId = requireNonBlank(fromLocationId, "From location ID");
        this.toLocationId = requireNonBlank(toLocationId, "To location ID");
        if (this.fromLocationId.equals(this.toLocationId)) {
            throw new IllegalArgumentException("Road endpoints must differ.");
        }

        this.distanceKm = requirePositiveFinite(distanceKm, "Distance");
        this.normalTravelTimeMinutes = requirePositiveFinite(normalTravelTimeMinutes, "Normal travel time");
        this.roadConditionWeight = requirePositiveFinite(roadConditionWeight, "Road condition weight");
        if (!Double.isFinite(this.normalTravelTimeMinutes * this.roadConditionWeight)) {
            throw new IllegalArgumentException("Effective time must be finite.");
        }
    }

    public String getRoadId() {
        return roadId;
    }

    public String getFromLocationId() {
        return fromLocationId;
    }

    public String getToLocationId() {
        return toLocationId;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getNormalTravelTimeMinutes() {
        return normalTravelTimeMinutes;
    }

    public double getRoadConditionWeight() {
        return roadConditionWeight;
    }

    /**
     * Returns the derived weighted travel time for this road.
     */
    public double getEffectiveTime() {
        return normalTravelTimeMinutes * roadConditionWeight;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Road road = (Road) other;
        return roadId.equals(road.roadId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roadId);
    }

    @Override
    public String toString() {
        return "Road{"
                + "roadId='" + roadId + '\''
                + ", fromLocationId='" + fromLocationId + '\''
                + ", toLocationId='" + toLocationId + '\''
                + ", distanceKm=" + distanceKm
                + '}';
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank.");
        }
        return value;
    }

    private static double requirePositiveFinite(double value, String fieldName) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(fieldName + " must be finite and greater than zero.");
        }
        return value;
    }
}
