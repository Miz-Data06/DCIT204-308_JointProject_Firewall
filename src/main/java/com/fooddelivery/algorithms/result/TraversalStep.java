package com.fooddelivery.algorithms.result;

/**
 * One traversal trace record.
 */
public final class TraversalStep {
    private final String locationId;
    private final String action;

    public TraversalStep(String locationId, String action) {
        if (locationId == null || locationId.isBlank()) {
            throw new IllegalArgumentException("Location ID must not be null or blank");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action must not be null or blank");
        }
        this.locationId = locationId;
        this.action = action;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getAction() {
        return action;
    }
}
