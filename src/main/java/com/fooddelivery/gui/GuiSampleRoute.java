package com.fooddelivery.gui;

public final class GuiSampleRoute {
    private final String sourceLocationId;
    private final String destinationLocationId;

    public GuiSampleRoute(String sourceLocationId, String destinationLocationId) {
        if (sourceLocationId == null || sourceLocationId.isBlank()) {
            throw new IllegalArgumentException("Sample source location ID is required");
        }
        if (destinationLocationId == null || destinationLocationId.isBlank()) {
            throw new IllegalArgumentException("Sample destination location ID is required");
        }
        this.sourceLocationId = sourceLocationId;
        this.destinationLocationId = destinationLocationId;
    }

    public String getSourceLocationId() {
        return sourceLocationId;
    }

    public String getDestinationLocationId() {
        return destinationLocationId;
    }
}
