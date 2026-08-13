package com.fooddelivery.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocationTest {
    @Test
    void constructorPreservesEveryField() {
        Location location = new Location(
                "LOC001",
                "Pizza King",
                "Greater Accra",
                LocationType.RESTAURANT,
                5.66268,
                -0.18161);

        assertEquals("LOC001", location.getLocationId());
        assertEquals("Pizza King", location.getName());
        assertEquals("Greater Accra", location.getArea());
        assertEquals(LocationType.RESTAURANT, location.getType());
        assertEquals(5.66268, location.getLatitude());
        assertEquals(-0.18161, location.getLongitude());
    }

    @Test
    void equalityUsesLocationId() {
        Location first = sampleLocation("LOC001");
        Location second = new Location("LOC001", "Other", "Other Area", LocationType.CAMPUS, 0.0, 0.0);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, sampleLocation("LOC002"));
    }

    @Test
    void toStringContainsLocationId() {
        assertTrue(sampleLocation("LOC001").toString().contains("LOC001"));
    }

    @Test
    void invalidRequiredFieldsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> locationWithId(null));
        assertThrows(IllegalArgumentException.class, () -> locationWithId(" "));
        assertThrows(IllegalArgumentException.class, () -> new Location(
                "LOC001", null, "Greater Accra", LocationType.RESTAURANT, 5.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new Location(
                "LOC001", " ", "Greater Accra", LocationType.RESTAURANT, 5.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new Location(
                "LOC001", "Pizza King", null, LocationType.RESTAURANT, 5.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new Location(
                "LOC001", "Pizza King", " ", LocationType.RESTAURANT, 5.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new Location(
                "LOC001", "Pizza King", "Greater Accra", null, 5.0, 0.0));
    }

    @Test
    void latitudeBoundariesAreAccepted() {
        assertEquals(-90.0, locationWithLatitude(-90.0).getLatitude());
        assertEquals(90.0, locationWithLatitude(90.0).getLatitude());
    }

    @Test
    void invalidLatitudeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> locationWithLatitude(-90.1));
        assertThrows(IllegalArgumentException.class, () -> locationWithLatitude(90.1));
        assertThrows(IllegalArgumentException.class, () -> locationWithLatitude(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> locationWithLatitude(Double.POSITIVE_INFINITY));
    }

    @Test
    void longitudeBoundariesAreAccepted() {
        assertEquals(-180.0, locationWithLongitude(-180.0).getLongitude());
        assertEquals(180.0, locationWithLongitude(180.0).getLongitude());
    }

    @Test
    void invalidLongitudeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> locationWithLongitude(-180.1));
        assertThrows(IllegalArgumentException.class, () -> locationWithLongitude(180.1));
        assertThrows(IllegalArgumentException.class, () -> locationWithLongitude(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> locationWithLongitude(Double.POSITIVE_INFINITY));
    }

    private static Location sampleLocation(String locationId) {
        return new Location(locationId, "Pizza King", "Greater Accra", LocationType.RESTAURANT, 5.66268, -0.18161);
    }

    private static Location locationWithId(String locationId) {
        return new Location(locationId, "Pizza King", "Greater Accra", LocationType.RESTAURANT, 5.0, 0.0);
    }

    private static Location locationWithLatitude(double latitude) {
        return new Location("LOC001", "Pizza King", "Greater Accra", LocationType.RESTAURANT, latitude, 0.0);
    }

    private static Location locationWithLongitude(double longitude) {
        return new Location("LOC001", "Pizza King", "Greater Accra", LocationType.RESTAURANT, 5.0, longitude);
    }
}
