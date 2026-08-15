package com.fooddelivery.datastructures.hashing;

import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.RequestStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomMapTest {
    @Test
    void defaultMapStartsEmptyWithHashTableCapacity() {
        CustomMap<String, String> map = new CustomMap<>();

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertEquals(11, map.capacity());
        assertEquals(0.0, map.loadFactor());
        assertNull(map.get("missing"));
        assertFalse(map.containsKey("missing"));
    }

    @Test
    void constructorDelegatesCapacityValidationAndNormalization() {
        assertEquals(2, new CustomMap<String, String>(1).capacity());
        assertEquals(5, new CustomMap<String, String>(4).capacity());
        assertEquals(17, new CustomMap<String, String>(17).capacity());
        assertThrows(IllegalArgumentException.class, () -> new CustomMap<String, String>(0));
        assertThrows(IllegalArgumentException.class, () -> new CustomMap<String, String>(-3));
    }

    @Test
    void rejectsNullKeysAndValues() {
        CustomMap<String, String> map = new CustomMap<>();

        assertThrows(IllegalArgumentException.class, () -> map.put(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> map.put("key", null));
        assertThrows(IllegalArgumentException.class, () -> map.get(null));
        assertThrows(IllegalArgumentException.class, () -> map.containsKey(null));
        assertThrows(IllegalArgumentException.class, () -> map.remove(null));
        assertTrue(map.isEmpty());
    }

    @Test
    void putGetContainsAndMissingLookupWorkForSeveralKeyTypes() {
        CustomMap<Object, String> map = new CustomMap<>();
        ControlledKey customKey = new ControlledKey("custom", 50);

        assertNull(map.put("REQ-001", "request"));
        assertNull(map.put(7, "integer"));
        assertNull(map.put(customKey, "custom"));

        assertEquals(3, map.size());
        assertFalse(map.isEmpty());
        assertEquals("request", map.get("REQ-001"));
        assertEquals("integer", map.get(7));
        assertEquals("custom", map.get(new ControlledKey("custom", 50)));
        assertTrue(map.containsKey("REQ-001"));
        assertTrue(map.containsKey(7));
        assertFalse(map.containsKey("missing"));
        assertNull(map.get("missing"));
    }

    @Test
    void duplicatePutsUpdateValueWithoutChangingSizeOrCapacity() {
        CustomMap<ControlledKey, String> map = new CustomMap<>(2);
        ControlledKey first = new ControlledKey("same", 8);
        ControlledKey equal = new ControlledKey("same", 8);

        assertNull(map.put(first, "one"));
        int capacity = map.capacity();

        assertEquals("one", map.put(equal, "two"));

        assertEquals(1, map.size());
        assertEquals(capacity, map.capacity());
        assertEquals("two", map.get(first));
        assertEquals("two", map.get(equal));
    }

    @Test
    void collidingKeysCoexistAndUpdateIndependently() {
        CustomMap<ControlledKey, String> map = new CustomMap<>(11);
        ControlledKey a = new ControlledKey("a", 42);
        ControlledKey b = new ControlledKey("b", 42);
        ControlledKey c = new ControlledKey("c", 42);

        map.put(a, "A");
        map.put(b, "B");
        map.put(c, "C");

        assertEquals(3, map.size());
        assertEquals("B", map.put(new ControlledKey("b", 42), "B2"));
        assertEquals("A", map.get(a));
        assertEquals("B2", map.get(b));
        assertEquals("C", map.get(c));
        assertNull(map.get(new ControlledKey("missing", 42)));
    }

    @Test
    void removeReturnsValueAndPreservesOtherCollidingMappings() {
        CustomMap<ControlledKey, String> map = new CustomMap<>(11);
        ControlledKey a = new ControlledKey("a", 77);
        ControlledKey b = new ControlledKey("b", 77);
        ControlledKey c = new ControlledKey("c", 77);
        map.put(a, "A");
        map.put(b, "B");
        map.put(c, "C");
        int capacity = map.capacity();

        assertEquals("B", map.remove(new ControlledKey("b", 77)));

        assertEquals(2, map.size());
        assertEquals(capacity, map.capacity());
        assertFalse(map.containsKey(b));
        assertNull(map.get(b));
        assertEquals("A", map.get(a));
        assertEquals("C", map.get(c));
    }

    @Test
    void missingRemoveThrowsAndLeavesStateUnchanged() {
        CustomMap<String, String> map = new CustomMap<>(5);
        map.put("a", "A");
        map.put("b", "B");
        int size = map.size();
        int capacity = map.capacity();

        assertThrows(IllegalArgumentException.class, () -> map.remove("missing"));

        assertEquals(size, map.size());
        assertEquals(capacity, map.capacity());
        assertEquals("A", map.get("a"));
        assertEquals("B", map.get("b"));
    }

    @Test
    void loadFactorAndResizingAreInheritedFromHashTable() {
        CustomMap<Integer, String> map = new CustomMap<>(2);

        map.put(1, "one");
        assertEquals(2, map.capacity());
        assertEquals(0.5, map.loadFactor(), 0.000001);

        map.put(2, "two");
        assertEquals(5, map.capacity());
        assertTrue(map.capacity() > 2 * 2);
        assertTrue(isPrime(map.capacity()));
        assertEquals(2.0 / 5.0, map.loadFactor(), 0.000001);
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
    }

    @Test
    void clearEmptiesMapPreservesCapacityAndAllowsReuse() {
        CustomMap<Integer, String> map = new CustomMap<>(3);
        for (int i = 1; i <= 10; i++) {
            map.put(i, "v" + i);
        }
        int capacity = map.capacity();

        map.clear();

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertEquals(capacity, map.capacity());
        assertEquals(0.0, map.loadFactor());
        assertNull(map.get(1));

        map.put(20, "twenty");
        assertEquals("twenty", map.get(20));
        assertEquals(1, map.size());
    }

    @Test
    void mapsDeliveryRequestsByRequestId() {
        CustomMap<String, DeliveryRequest> map = new CustomMap<>(3);
        DeliveryRequest first = request("REQ-001", 1);
        DeliveryRequest second = request("REQ-002", 2);
        DeliveryRequest replacement = request("REQ-002", 22);

        assertNull(map.put(first.getRequestId(), first));
        assertNull(map.put(second.getRequestId(), second));
        assertSame(first, map.get("REQ-001"));
        assertSame(second, map.get("REQ-002"));
        assertSame(second, map.put("REQ-002", replacement));
        assertSame(replacement, map.get("REQ-002"));

        DeliveryRequest removed = map.remove("REQ-002");
        assertSame(replacement, removed);
        assertEquals("REQ-002", removed.getRequestId());
        assertNull(map.get("REQ-002"));
        assertSame(first, map.get("REQ-001"));
    }

    @Test
    void multipleDeliveryRequestMappingsSurviveResizing() {
        CustomMap<String, DeliveryRequest> map = new CustomMap<>(2);
        DeliveryRequest[] requests = new DeliveryRequest[24];

        for (int i = 0; i < requests.length; i++) {
            String requestId = requestId(i + 1);
            requests[i] = request(requestId, i + 1);
            map.put(requestId, requests[i]);
        }

        assertEquals(requests.length, map.size());
        assertTrue(map.capacity() > 23);
        for (int i = 0; i < requests.length; i++) {
            assertSame(requests[i], map.get(requestId(i + 1)));
        }
    }

    @Test
    void largerDeterministicSequenceRemainsCorrect() {
        CustomMap<Integer, String> map = new CustomMap<>(2);

        for (int i = 1; i <= 80; i++) {
            map.put(i, "v" + i);
        }
        for (int i = 8; i <= 80; i += 8) {
            assertEquals("v" + i, map.put(i, "updated-" + i));
        }
        for (int i = 1; i <= 20; i += 2) {
            assertEquals("v" + i, map.remove(i));
        }

        assertEquals(70, map.size());
        assertTrue(isPrime(map.capacity()));
        for (int i = 1; i <= 20; i += 2) {
            assertFalse(map.containsKey(i));
            assertNull(map.get(i));
        }
        for (int i = 2; i <= 80; i += 2) {
            assertTrue(map.containsKey(i));
            if (i % 8 == 0) {
                assertEquals("updated-" + i, map.get(i));
            }
        }
    }

    private DeliveryRequest request(String requestId, int index) {
        LocalDateTime submitted = LocalDateTime.of(2026, 1, 1, 11, 0).plusMinutes(index);
        return new DeliveryRequest(
                requestId,
                "LOC-" + index,
                "LOC-" + (index + 1),
                "GROCERY",
                (index % 5) + 1,
                2.0 + index,
                submitted,
                submitted.plusHours(2),
                RequestStatus.PENDING,
                4.0 + index
        );
    }

    private String requestId(int number) {
        if (number < 10) {
            return "REQ-00" + number;
        }
        return "REQ-0" + number;
    }

    private boolean isPrime(int value) {
        if (value < 2) {
            return false;
        }
        if (value == 2) {
            return true;
        }
        if (value % 2 == 0) {
            return false;
        }
        for (int divisor = 3; divisor <= value / divisor; divisor += 2) {
            if (value % divisor == 0) {
                return false;
            }
        }
        return true;
    }

    private static final class ControlledKey {
        private final String id;
        private final int hash;

        private ControlledKey(String id, int hash) {
            this.id = id;
            this.hash = hash;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ControlledKey that)) {
                return false;
            }
            return id.equals(that.id);
        }
    }
}
