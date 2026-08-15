package com.fooddelivery.model;

import java.util.Objects;

/**
 * Immutable shared location model for delivery routing and dataset integration.
 */
public class Location {
    private final String locationId;
    private final String name;
    private final String area;
    private final LocationType type;
    private final double latitude;
    private final double longitude;

    /**
     * Creates a validated location with fixed identity, category and coordinates.
     */
    public Location(
            String locationId,
            String name,
            String area,
            LocationType type,
            double latitude,
            double longitude) {
        this.locationId = requireNonBlank(locationId, "Location ID");
        this.name = requireNonBlank(name, "Name");
        this.area = requireNonBlank(area, "Area");
        this.type = requireNonNull(type, "Location type");
        this.latitude = requireCoordinate(latitude, -90.0, 90.0, "Latitude");
        this.longitude = requireCoordinate(longitude, -180.0, 180.0, "Longitude");
    }

    public String getLocationId() {
        return locationId;
    }

    public String getName() {
        return name;
    }

    public String getArea() {
        return area;
    }

    public LocationType getType() {
        return type;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Location location = (Location) other;
        return locationId.equals(location.locationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationId);
    }

    @Override
    public String toString() {
        return "Location{"
                + "locationId='" + locationId + '\''
                + ", name='" + name + '\''
                + ", area='" + area + '\''
                + ", type=" + type
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

    private static double requireCoordinate(double value, double minimum, double maximum, String fieldName) {
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            throw new IllegalArgumentException(fieldName + " must be finite and within the valid coordinate range.");
        }
        return value;
    }
}
