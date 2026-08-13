package com.fooddelivery.algorithms.result;

/**
 * One Dijkstra trace record for testing and later trace-table evidence.
 */
public final class DijkstraStep {
    private final String currentLocationId;
    private final String neighbourLocationId;
    private final double tentativeTime;
    private final String predecessorLocationId;
    private final boolean updated;

    public DijkstraStep(
            String currentLocationId,
            String neighbourLocationId,
            double tentativeTime,
            String predecessorLocationId,
            boolean updated) {
        this.currentLocationId = currentLocationId;
        this.neighbourLocationId = neighbourLocationId;
        this.tentativeTime = tentativeTime;
        this.predecessorLocationId = predecessorLocationId;
        this.updated = updated;
    }

    public String getCurrentLocationId() {
        return currentLocationId;
    }

    public String getNeighbourLocationId() {
        return neighbourLocationId;
    }

    public double getTentativeTime() {
        return tentativeTime;
    }

    public String getPredecessorLocationId() {
        return predecessorLocationId;
    }

    public boolean isUpdated() {
        return updated;
    }
}
