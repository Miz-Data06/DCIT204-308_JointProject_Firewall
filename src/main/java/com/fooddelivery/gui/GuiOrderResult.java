package com.fooddelivery.gui;

import com.fooddelivery.model.RequestStatus;

public final class GuiOrderResult {
    private final String requestId;
    private final String sourceLabel;
    private final String destinationLabel;
    private final String category;
    private final String urgencyLabel;
    private final double capacityRequired;
    private final RequestStatus status;
    private final double priorityScore;
    private final boolean routeAvailable;
    private final String routePath;
    private final double effectiveTravelTime;
    private final boolean riderAssigned;
    private final String assignedRiderLabel;
    private final String message;

    public GuiOrderResult(
            String requestId,
            String sourceLabel,
            String destinationLabel,
            String category,
            String urgencyLabel,
            double capacityRequired,
            RequestStatus status,
            double priorityScore,
            boolean routeAvailable,
            String routePath,
            double effectiveTravelTime,
            boolean riderAssigned,
            String assignedRiderLabel,
            String message) {
        this.requestId = requestId;
        this.sourceLabel = sourceLabel;
        this.destinationLabel = destinationLabel;
        this.category = category;
        this.urgencyLabel = urgencyLabel;
        this.capacityRequired = capacityRequired;
        this.status = status;
        this.priorityScore = priorityScore;
        this.routeAvailable = routeAvailable;
        this.routePath = routePath;
        this.effectiveTravelTime = effectiveTravelTime;
        this.riderAssigned = riderAssigned;
        this.assignedRiderLabel = assignedRiderLabel;
        this.message = message;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public String getDestinationLabel() {
        return destinationLabel;
    }

    public String getCategory() {
        return category;
    }

    public String getUrgencyLabel() {
        return urgencyLabel;
    }

    public double getCapacityRequired() {
        return capacityRequired;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    public boolean isRouteAvailable() {
        return routeAvailable;
    }

    public String getRoutePath() {
        return routePath;
    }

    public double getEffectiveTravelTime() {
        return effectiveTravelTime;
    }

    public boolean isRiderAssigned() {
        return riderAssigned;
    }

    public String getAssignedRiderLabel() {
        return assignedRiderLabel;
    }

    public String getMessage() {
        return message;
    }

    public String getSummary() {
        String routeSummary = routeAvailable
                ? "Route ready, " + String.format("%.2f", effectiveTravelTime) + " effective time"
                : "Route unavailable";
        String riderSummary = riderAssigned ? "assigned" : "no rider assigned";
        return routeSummary + "; " + riderSummary;
    }
}
