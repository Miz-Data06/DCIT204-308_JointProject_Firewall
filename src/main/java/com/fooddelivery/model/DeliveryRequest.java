package com.fooddelivery.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Shared delivery request model used by dispatch data structures and algorithms.
 * Identity and routing fields are fixed after construction; status and priority
 * score may change as dispatch progresses.
 */
public class DeliveryRequest {
    private final String requestId;
    private final String sourceLocationId;
    private final String destinationLocationId;
    private final String category;
    private final int urgency;
    private final double capacityRequired;
    private final LocalDateTime timeSubmitted;
    private final LocalDateTime deadline;
    private RequestStatus status;
    private double priorityScore;

    /**
     * Creates a validated delivery request with fixed identity, routing and timing data.
     */
    public DeliveryRequest(
            String requestId,
            String sourceLocationId,
            String destinationLocationId,
            String category,
            int urgency,
            double capacityRequired,
            LocalDateTime timeSubmitted,
            LocalDateTime deadline,
            RequestStatus status,
            double priorityScore) {
        this.requestId = requireNonBlank(requestId, "Request ID");
        this.sourceLocationId = requireNonBlank(sourceLocationId, "Source location ID");
        this.destinationLocationId = requireNonBlank(destinationLocationId, "Destination location ID");
        if (this.sourceLocationId.equals(this.destinationLocationId)) {
            throw new IllegalArgumentException("Source and destination location IDs must differ.");
        }

        this.category = requireNonBlank(category, "Category");
        if (urgency <= 0) {
            throw new IllegalArgumentException("Urgency must be positive.");
        }
        if (!Double.isFinite(capacityRequired) || capacityRequired <= 0.0) {
            throw new IllegalArgumentException("Capacity required must be finite and greater than zero.");
        }

        this.timeSubmitted = requireNonNull(timeSubmitted, "Time submitted");
        this.deadline = requireNonNull(deadline, "Deadline");
        if (!deadline.isAfter(timeSubmitted)) {
            throw new IllegalArgumentException("Deadline must be strictly after time submitted.");
        }

        this.status = requireNonNull(status, "Status");
        validatePriorityScore(priorityScore);

        this.urgency = urgency;
        this.capacityRequired = capacityRequired;
        this.priorityScore = priorityScore;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getSourceLocationId() {
        return sourceLocationId;
    }

    public String getDestinationLocationId() {
        return destinationLocationId;
    }

    public String getCategory() {
        return category;
    }

    public int getUrgency() {
        return urgency;
    }

    public double getCapacityRequired() {
        return capacityRequired;
    }

    public LocalDateTime getTimeSubmitted() {
        return timeSubmitted;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public RequestStatus getStatus() {
        return status;
    }

    /**
     * Updates the request lifecycle status.
     */
    public void setStatus(RequestStatus status) {
        this.status = requireNonNull(status, "Status");
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    /**
     * Updates the externally calculated dispatch priority score.
     */
    public void setPriorityScore(double priorityScore) {
        validatePriorityScore(priorityScore);
        this.priorityScore = priorityScore;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        DeliveryRequest that = (DeliveryRequest) other;
        return requestId.equals(that.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId);
    }

    @Override
    public String toString() {
        return "DeliveryRequest{"
                + "requestId='" + requestId + '\''
                + ", sourceLocationId='" + sourceLocationId + '\''
                + ", destinationLocationId='" + destinationLocationId + '\''
                + ", status=" + status
                + ", priorityScore=" + priorityScore
                + '}';
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank.");
        }
        return value;
    }

    private static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null.");
        }
        return value;
    }

    private static void validatePriorityScore(double priorityScore) {
        if (!Double.isFinite(priorityScore)) {
            throw new IllegalArgumentException("Priority score must be finite.");
        }
    }
}
