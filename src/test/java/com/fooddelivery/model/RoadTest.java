package com.fooddelivery.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoadTest {
    @Test
    void constructorPreservesEveryField() {
        Road road = new Road("RD001", "LOC001", "LOC002", 3.5, 10.0, 1.2);

        assertEquals("RD001", road.getRoadId());
        assertEquals("LOC001", road.getFromLocationId());
        assertEquals("LOC002", road.getToLocationId());
        assertEquals(3.5, road.getDistanceKm());
        assertEquals(10.0, road.getNormalTravelTimeMinutes());
        assertEquals(1.2, road.getRoadConditionWeight());
    }

    @Test
    void effectiveTimeIsCalculatedFromTravelTimeAndConditionWeight() {
        assertEquals(12.0, new Road("RD001", "LOC001", "LOC002", 3.5, 10.0, 1.2).getEffectiveTime());
    }

    @Test
    void equalityUsesRoadId() {
        Road first = sampleRoad("RD001");
        Road second = new Road("RD001", "LOC003", "LOC004", 9.0, 11.0, 2.0);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, sampleRoad("RD002"));
    }

    @Test
    void toStringContainsRoadId() {
        assertTrue(sampleRoad("RD001").toString().contains("RD001"));
    }

    @Test
    void invalidRequiredFieldsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> roadWithId(null));
        assertThrows(IllegalArgumentException.class, () -> roadWithId(" "));
        assertThrows(IllegalArgumentException.class, () -> new Road("RD001", null, "LOC002", 1.0, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Road("RD001", " ", "LOC002", 1.0, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Road("RD001", "LOC001", null, 1.0, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Road("RD001", "LOC001", " ", 1.0, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Road("RD001", "LOC001", "LOC001", 1.0, 1.0, 1.0));
    }

    @Test
    void invalidDistanceIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> roadWithDistance(0.0));
        assertThrows(IllegalArgumentException.class, () -> roadWithDistance(-1.0));
        assertThrows(IllegalArgumentException.class, () -> roadWithDistance(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> roadWithDistance(Double.POSITIVE_INFINITY));
    }

    @Test
    void invalidNormalTravelTimeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> roadWithNormalTravelTime(0.0));
        assertThrows(IllegalArgumentException.class, () -> roadWithNormalTravelTime(-1.0));
        assertThrows(IllegalArgumentException.class, () -> roadWithNormalTravelTime(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> roadWithNormalTravelTime(Double.POSITIVE_INFINITY));
    }

    @Test
    void invalidConditionWeightIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> roadWithConditionWeight(0.0));
        assertThrows(IllegalArgumentException.class, () -> roadWithConditionWeight(-1.0));
        assertThrows(IllegalArgumentException.class, () -> roadWithConditionWeight(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> roadWithConditionWeight(Double.POSITIVE_INFINITY));
    }

    @Test
    void overflowingEffectiveTimeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Road(
                "RD001", "LOC001", "LOC002", 1.0, Double.MAX_VALUE, 2.0));
    }

    private static Road sampleRoad(String roadId) {
        return new Road(roadId, "LOC001", "LOC002", 3.5, 10.0, 1.2);
    }

    private static Road roadWithId(String roadId) {
        return new Road(roadId, "LOC001", "LOC002", 1.0, 1.0, 1.0);
    }

    private static Road roadWithDistance(double distanceKm) {
        return new Road("RD001", "LOC001", "LOC002", distanceKm, 1.0, 1.0);
    }

    private static Road roadWithNormalTravelTime(double normalTravelTimeMinutes) {
        return new Road("RD001", "LOC001", "LOC002", 1.0, normalTravelTimeMinutes, 1.0);
    }

    private static Road roadWithConditionWeight(double roadConditionWeight) {
        return new Road("RD001", "LOC001", "LOC002", 1.0, 1.0, roadConditionWeight);
    }
}
