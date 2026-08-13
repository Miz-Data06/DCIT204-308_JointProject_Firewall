package com.fooddelivery.datastructures.graphs;

import java.util.Objects;

/**
 * Immutable directed outgoing edge view for an undirected road.
 */
public final class Edge {
    private final String roadId;
    private final String sourceId;
    private final String destinationId;
    private final double distanceKm;
    private final double normalTravelTimeMinutes;
    private final double roadConditionWeight;

    /**
     * Creates an immutable outgoing edge.
     *
     * @param roadId non-blank road identity
     * @param sourceId non-blank source location identity
     * @param destinationId non-blank destination location identity
     * @param distanceKm positive finite distance
     * @param normalTravelTimeMinutes positive finite normal travel time
     * @param roadConditionWeight positive finite road-condition multiplier
     */
    public Edge(
            String roadId,
            String sourceId,
            String destinationId,
            double distanceKm,
            double normalTravelTimeMinutes,
            double roadConditionWeight) {
        this.roadId = requireNonBlank(roadId, "Road ID");
        this.sourceId = requireNonBlank(sourceId, "Source ID");
        this.destinationId = requireNonBlank(destinationId, "Destination ID");
        if (this.sourceId.equals(this.destinationId)) {
            throw new IllegalArgumentException("Edge endpoints must differ");
        }

        this.distanceKm = requirePositiveFinite(distanceKm, "Distance");
        this.normalTravelTimeMinutes = requirePositiveFinite(normalTravelTimeMinutes, "Normal travel time");
        this.roadConditionWeight = requirePositiveFinite(roadConditionWeight, "Road condition weight");
        if (!Double.isFinite(this.normalTravelTimeMinutes * this.roadConditionWeight)) {
            throw new IllegalArgumentException("Effective time must be finite");
        }
    }

    public String getRoadId() {
        return roadId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getDestinationId() {
        return destinationId;
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

    public double getEffectiveTime() {
        return normalTravelTimeMinutes * roadConditionWeight;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Edge edge)) {
            return false;
        }
        return roadId.equals(edge.roadId)
                && sourceId.equals(edge.sourceId)
                && destinationId.equals(edge.destinationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roadId, sourceId, destinationId);
    }

    @Override
    public String toString() {
        return "Edge{"
                + "roadId='" + roadId + '\''
                + ", sourceId='" + sourceId + '\''
                + ", destinationId='" + destinationId + '\''
                + '}';
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
        return value;
    }

    private static double requirePositiveFinite(double value, String fieldName) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(fieldName + " must be finite and greater than zero");
        }
        return value;
    }
}
