package com.fooddelivery.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeliveryRequestTest {
    private static final LocalDateTime SUBMITTED = LocalDateTime.of(2026, 8, 13, 9, 0);
    private static final LocalDateTime DEADLINE = SUBMITTED.plusHours(2);

    @Test
    void constructorPreservesEveryField() {
        DeliveryRequest request = new DeliveryRequest(
                "REQ001",
                "LOC001",
                "LOC002",
                "Food Delivery",
                3,
                12.5,
                SUBMITTED,
                DEADLINE,
                RequestStatus.PENDING,
                45.75);

        assertEquals("REQ001", request.getRequestId());
        assertEquals("LOC001", request.getSourceLocationId());
        assertEquals("LOC002", request.getDestinationLocationId());
        assertEquals("Food Delivery", request.getCategory());
        assertEquals(3, request.getUrgency());
        assertEquals(12.5, request.getCapacityRequired());
        assertEquals(SUBMITTED, request.getTimeSubmitted());
        assertEquals(DEADLINE, request.getDeadline());
        assertEquals(RequestStatus.PENDING, request.getStatus());
        assertEquals(45.75, request.getPriorityScore());
    }

    @Test
    void statusCanBeUpdated() {
        DeliveryRequest request = sampleRequest("REQ001");

        request.setStatus(RequestStatus.ASSIGNED);

        assertEquals(RequestStatus.ASSIGNED, request.getStatus());
    }

    @Test
    void priorityScoreCanBeUpdated() {
        DeliveryRequest request = sampleRequest("REQ001");

        request.setPriorityScore(-5.5);

        assertEquals(-5.5, request.getPriorityScore());
    }

    @Test
    void equalityUsesRequestId() {
        DeliveryRequest first = sampleRequest("REQ001");
        DeliveryRequest second = new DeliveryRequest(
                "REQ001",
                "LOC003",
                "LOC004",
                "Medicine",
                8,
                2.5,
                SUBMITTED.plusMinutes(5),
                DEADLINE.plusHours(1),
                RequestStatus.ASSIGNED,
                99.0);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, sampleRequest("REQ002"));
    }

    @Test
    void mutableFieldsDoNotAffectEqualityOrHashCode() {
        DeliveryRequest first = sampleRequest("REQ001");
        DeliveryRequest second = sampleRequest("REQ001");
        int hashCode = first.hashCode();

        first.setStatus(RequestStatus.PICKED_UP);
        first.setPriorityScore(200.0);

        assertEquals(first, second);
        assertEquals(hashCode, first.hashCode());
    }

    @Test
    void toStringContainsRequestId() {
        assertTrue(sampleRequest("REQ001").toString().contains("REQ001"));
    }

    @Test
    void invalidStringFieldsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> requestWithId(null));
        assertThrows(IllegalArgumentException.class, () -> requestWithId(" "));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(
                "REQ001", null, "LOC002", "Food", 1, 1.0, SUBMITTED, DEADLINE, RequestStatus.PENDING, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(
                "REQ001", " ", "LOC002", "Food", 1, 1.0, SUBMITTED, DEADLINE, RequestStatus.PENDING, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(
                "REQ001", "LOC001", null, "Food", 1, 1.0, SUBMITTED, DEADLINE, RequestStatus.PENDING, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(
                "REQ001", "LOC001", " ", "Food", 1, 1.0, SUBMITTED, DEADLINE, RequestStatus.PENDING, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(
                "REQ001", "LOC001", "LOC001", "Food", 1, 1.0, SUBMITTED, DEADLINE, RequestStatus.PENDING, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(
                "REQ001", "LOC001", "LOC002", null, 1, 1.0, SUBMITTED, DEADLINE, RequestStatus.PENDING, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(
                "REQ001", "LOC001", "LOC002", " ", 1, 1.0, SUBMITTED, DEADLINE, RequestStatus.PENDING, 0.0));
    }

    @Test
    void invalidUrgencyIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> requestWithUrgency(0));
        assertThrows(IllegalArgumentException.class, () -> requestWithUrgency(-1));
    }

    @Test
    void invalidCapacityIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> requestWithCapacity(0.0));
        assertThrows(IllegalArgumentException.class, () -> requestWithCapacity(-1.0));
        assertThrows(IllegalArgumentException.class, () -> requestWithCapacity(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> requestWithCapacity(Double.POSITIVE_INFINITY));
    }

    @Test
    void invalidDatesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(
                "REQ001", "LOC001", "LOC002", "Food", 1, 1.0, null, DEADLINE, RequestStatus.PENDING, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(
                "REQ001", "LOC001", "LOC002", "Food", 1, 1.0, SUBMITTED, null, RequestStatus.PENDING, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(
                "REQ001", "LOC001", "LOC002", "Food", 1, 1.0, SUBMITTED, SUBMITTED, RequestStatus.PENDING, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(
                "REQ001", "LOC001", "LOC002", "Food", 1, 1.0, SUBMITTED, SUBMITTED.minusMinutes(1), RequestStatus.PENDING, 0.0));
    }

    @Test
    void invalidStatusIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(
                "REQ001", "LOC001", "LOC002", "Food", 1, 1.0, SUBMITTED, DEADLINE, null, 0.0));
        assertThrows(IllegalArgumentException.class, () -> sampleRequest("REQ001").setStatus(null));
    }

    @Test
    void invalidPriorityIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> requestWithPriority(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> requestWithPriority(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> requestWithPriority(Double.NEGATIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> sampleRequest("REQ001").setPriorityScore(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> sampleRequest("REQ001").setPriorityScore(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> sampleRequest("REQ001").setPriorityScore(Double.NEGATIVE_INFINITY));
    }

    private static DeliveryRequest sampleRequest(String requestId) {
        return new DeliveryRequest(
                requestId,
                "LOC001",
                "LOC002",
                "Food Delivery",
                3,
                12.5,
                SUBMITTED,
                DEADLINE,
                RequestStatus.PENDING,
                45.75);
    }

    private static DeliveryRequest requestWithId(String requestId) {
        return new DeliveryRequest(
                requestId, "LOC001", "LOC002", "Food", 1, 1.0, SUBMITTED, DEADLINE, RequestStatus.PENDING, 0.0);
    }

    private static DeliveryRequest requestWithUrgency(int urgency) {
        return new DeliveryRequest(
                "REQ001", "LOC001", "LOC002", "Food", urgency, 1.0, SUBMITTED, DEADLINE, RequestStatus.PENDING, 0.0);
    }

    private static DeliveryRequest requestWithCapacity(double capacityRequired) {
        return new DeliveryRequest(
                "REQ001", "LOC001", "LOC002", "Food", 1, capacityRequired, SUBMITTED, DEADLINE, RequestStatus.PENDING, 0.0);
    }

    private static DeliveryRequest requestWithPriority(double priorityScore) {
        return new DeliveryRequest(
                "REQ001", "LOC001", "LOC002", "Food", 1, 1.0, SUBMITTED, DEADLINE, RequestStatus.PENDING, priorityScore);
    }
}
