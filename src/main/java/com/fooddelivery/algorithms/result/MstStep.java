package com.fooddelivery.algorithms.result;

/**
 * One MST trace decision for Prim or Kruskal.
 */
public final class MstStep {
    private final String roadId;
    private final String fromLocationId;
    private final String toLocationId;
    private final double effectiveCost;
    private final boolean selected;
    private final String reason;

    public MstStep(
            String roadId,
            String fromLocationId,
            String toLocationId,
            double effectiveCost,
            boolean selected,
            String reason) {
        this.roadId = roadId;
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.effectiveCost = effectiveCost;
        this.selected = selected;
        this.reason = reason;
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

    public double getEffectiveCost() {
        return effectiveCost;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getReason() {
        return reason;
    }
}
