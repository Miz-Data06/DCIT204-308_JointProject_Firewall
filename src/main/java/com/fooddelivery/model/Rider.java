package com.fooddelivery.model;

import java.util.Objects;

/**
 * Shared rider model for assignment, routing and availability tracking.
 * Identity, name, vehicle type and capacity are fixed after construction.
 */
public class Rider {
    private final String riderId;
    private final String name;
    private String currentLocationId;
    private final VehicleType vehicleType;
    private final double carryingCapacity;
    private boolean available;

    /**
     * Creates a validated rider with mutable current location and availability.
     */
    public Rider(
            String riderId,
            String name,
            String currentLocationId,
            VehicleType vehicleType,
            double carryingCapacity,
            boolean available) {
        this.riderId = requireNonBlank(riderId, "Rider ID");
        this.name = requireNonBlank(name, "Name");
        this.currentLocationId = requireNonBlank(currentLocationId, "Current location ID");
        this.vehicleType = requireNonNull(vehicleType, "Vehicle type");
        this.carryingCapacity = requirePositiveFinite(carryingCapacity, "Carrying capacity");
        this.available = available;
    }

    public String getRiderId() {
        return riderId;
    }

    public String getName() {
        return name;
    }

    public String getCurrentLocationId() {
        return currentLocationId;
    }

    /**
     * Updates the rider's current location after movement.
     */
    public void setCurrentLocationId(String currentLocationId) {
        this.currentLocationId = requireNonBlank(currentLocationId, "Current location ID");
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public double getCarryingCapacity() {
        return carryingCapacity;
    }

    public boolean isAvailable() {
        return available;
    }

    /**
     * Updates whether the rider can receive a delivery assignment.
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Rider rider = (Rider) other;
        return riderId.equals(rider.riderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(riderId);
    }

    @Override
    public String toString() {
        return "Rider{"
                + "riderId='" + riderId + '\''
                + ", name='" + name + '\''
                + ", currentLocationId='" + currentLocationId + '\''
                + ", vehicleType=" + vehicleType
                + ", available=" + available
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

    private static double requirePositiveFinite(double value, String fieldName) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(fieldName + " must be finite and greater than zero.");
        }
        return value;
    }
}
