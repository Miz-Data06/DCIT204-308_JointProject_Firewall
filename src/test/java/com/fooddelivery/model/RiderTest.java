package com.fooddelivery.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiderTest {
    @Test
    void constructorPreservesEveryField() {
        Rider rider = new Rider("RID001", "Rider One", "LOC001", VehicleType.MOTORCYCLE, 15.0, true);

        assertEquals("RID001", rider.getRiderId());
        assertEquals("Rider One", rider.getName());
        assertEquals("LOC001", rider.getCurrentLocationId());
        assertEquals(VehicleType.MOTORCYCLE, rider.getVehicleType());
        assertEquals(15.0, rider.getCarryingCapacity());
        assertTrue(rider.isAvailable());
    }

    @Test
    void availableAndUnavailableStatesAreRepresented() {
        assertTrue(new Rider("RID001", "Rider One", "LOC001", VehicleType.MOTORCYCLE, 15.0, true).isAvailable());
        assertFalse(new Rider("RID002", "Rider Two", "LOC002", VehicleType.CAR, 30.0, false).isAvailable());
    }

    @Test
    void currentLocationCanBeUpdated() {
        Rider rider = sampleRider("RID001");

        rider.setCurrentLocationId("LOC099");

        assertEquals("LOC099", rider.getCurrentLocationId());
    }

    @Test
    void availabilityCanBeUpdated() {
        Rider rider = sampleRider("RID001");

        rider.setAvailable(false);

        assertFalse(rider.isAvailable());
    }

    @Test
    void equalityUsesRiderId() {
        Rider first = sampleRider("RID001");
        Rider second = new Rider("RID001", "Different Name", "LOC010", VehicleType.CARGO_VEHICLE, 50.0, false);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, sampleRider("RID002"));
    }

    @Test
    void mutableFieldsDoNotAffectEqualityOrHashCode() {
        Rider first = sampleRider("RID001");
        Rider second = sampleRider("RID001");
        int hashCode = first.hashCode();

        first.setCurrentLocationId("LOC090");
        first.setAvailable(false);

        assertEquals(first, second);
        assertEquals(hashCode, first.hashCode());
    }

    @Test
    void toStringContainsRiderId() {
        assertTrue(sampleRider("RID001").toString().contains("RID001"));
    }

    @Test
    void invalidRequiredFieldsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> riderWithId(null));
        assertThrows(IllegalArgumentException.class, () -> riderWithId(" "));
        assertThrows(IllegalArgumentException.class, () -> new Rider(
                "RID001", null, "LOC001", VehicleType.MOTORCYCLE, 15.0, true));
        assertThrows(IllegalArgumentException.class, () -> new Rider(
                "RID001", " ", "LOC001", VehicleType.MOTORCYCLE, 15.0, true));
        assertThrows(IllegalArgumentException.class, () -> new Rider(
                "RID001", "Rider One", null, VehicleType.MOTORCYCLE, 15.0, true));
        assertThrows(IllegalArgumentException.class, () -> new Rider(
                "RID001", "Rider One", " ", VehicleType.MOTORCYCLE, 15.0, true));
        assertThrows(IllegalArgumentException.class, () -> new Rider(
                "RID001", "Rider One", "LOC001", null, 15.0, true));
    }

    @Test
    void invalidCapacityIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> riderWithCapacity(0.0));
        assertThrows(IllegalArgumentException.class, () -> riderWithCapacity(-1.0));
        assertThrows(IllegalArgumentException.class, () -> riderWithCapacity(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> riderWithCapacity(Double.POSITIVE_INFINITY));
    }

    @Test
    void invalidCurrentLocationUpdatesAreRejected() {
        Rider rider = sampleRider("RID001");

        assertThrows(IllegalArgumentException.class, () -> rider.setCurrentLocationId(null));
        assertThrows(IllegalArgumentException.class, () -> rider.setCurrentLocationId(" "));
    }

    private static Rider sampleRider(String riderId) {
        return new Rider(riderId, "Rider One", "LOC001", VehicleType.MOTORCYCLE, 15.0, true);
    }

    private static Rider riderWithId(String riderId) {
        return new Rider(riderId, "Rider One", "LOC001", VehicleType.MOTORCYCLE, 15.0, true);
    }

    private static Rider riderWithCapacity(double carryingCapacity) {
        return new Rider("RID001", "Rider One", "LOC001", VehicleType.MOTORCYCLE, carryingCapacity, true);
    }
}
