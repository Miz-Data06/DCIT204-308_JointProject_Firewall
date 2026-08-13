package com.fooddelivery.datastructures.graphs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EdgeTest {
    @Test
    void constructorStoresFieldsAndComputesEffectiveTime() {
        Edge edge = new Edge("R-1", "LOC-A", "LOC-B", 4.5, 12.0, 1.25);

        assertEquals("R-1", edge.getRoadId());
        assertEquals("LOC-A", edge.getSourceId());
        assertEquals("LOC-B", edge.getDestinationId());
        assertEquals(4.5, edge.getDistanceKm());
        assertEquals(12.0, edge.getNormalTravelTimeMinutes());
        assertEquals(1.25, edge.getRoadConditionWeight());
        assertEquals(15.0, edge.getEffectiveTime());
    }

    @Test
    void directedIdentityUsesRoadSourceAndDestination() {
        Edge first = new Edge("R-1", "LOC-A", "LOC-B", 4.5, 12.0, 1.25);
        Edge sameIdentity = new Edge("R-1", "LOC-A", "LOC-B", 9.0, 30.0, 2.0);
        Edge reverse = new Edge("R-1", "LOC-B", "LOC-A", 4.5, 12.0, 1.25);
        Edge differentRoad = new Edge("R-2", "LOC-A", "LOC-B", 4.5, 12.0, 1.25);

        assertEquals(first, sameIdentity);
        assertEquals(first.hashCode(), sameIdentity.hashCode());
        assertNotEquals(first, reverse);
        assertNotEquals(first, differentRoad);
        assertFalse(first.equals("not-an-edge"));
    }

    @Test
    void toStringContainsRoadAndEndpointIdentities() {
        String text = new Edge("R-1", "LOC-A", "LOC-B", 4.5, 12.0, 1.25).toString();

        assertTrue(text.contains("R-1"));
        assertTrue(text.contains("LOC-A"));
        assertTrue(text.contains("LOC-B"));
    }

    @Test
    void rejectsInvalidIdentities() {
        assertThrows(IllegalArgumentException.class, () -> new Edge(null, "LOC-A", "LOC-B", 1.0, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Edge(" ", "LOC-A", "LOC-B", 1.0, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Edge("R-1", null, "LOC-B", 1.0, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Edge("R-1", " ", "LOC-B", 1.0, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Edge("R-1", "LOC-A", null, 1.0, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Edge("R-1", "LOC-A", " ", 1.0, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Edge("R-1", "LOC-A", "LOC-A", 1.0, 1.0, 1.0));
    }

    @Test
    void rejectsInvalidMeasurementsAndEffectiveTimeOverflow() {
        assertThrows(IllegalArgumentException.class, () -> new Edge("R-1", "LOC-A", "LOC-B", 0.0, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Edge("R-1", "LOC-A", "LOC-B", -1.0, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Edge("R-1", "LOC-A", "LOC-B", Double.NaN, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Edge("R-1", "LOC-A", "LOC-B", Double.POSITIVE_INFINITY, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Edge("R-1", "LOC-A", "LOC-B", 1.0, 0.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Edge("R-1", "LOC-A", "LOC-B", 1.0, 1.0, -1.0));
        assertThrows(IllegalArgumentException.class, () -> new Edge("R-1", "LOC-A", "LOC-B", 1.0, Double.MAX_VALUE, 2.0));
    }
}
