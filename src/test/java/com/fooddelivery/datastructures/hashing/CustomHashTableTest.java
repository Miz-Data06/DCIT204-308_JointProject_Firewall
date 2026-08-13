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

class CustomHashTableTest {
    @Test
    void defaultTableStartsEmptyWithPrimeCapacity() {
        CustomHashTable<String, String> table = new CustomHashTable<>();

        assertTrue(table.isEmpty());
        assertEquals(0, table.size());
        assertEquals(11, table.capacity());
        assertEquals(0.0, table.loadFactor());
        assertNull(table.get("missing"));
        assertFalse(table.containsKey("missing"));
    }

    @Test
    void constructorPreservesPrimeAndNormalizesNonPrimeCapacities() {
        assertEquals(2, new CustomHashTable<String, String>(1).capacity());
        assertEquals(2, new CustomHashTable<String, String>(2).capacity());
        assertEquals(3, new CustomHashTable<String, String>(3).capacity());
        assertEquals(5, new CustomHashTable<String, String>(4).capacity());
        assertEquals(11, new CustomHashTable<String, String>(10).capacity());
        assertEquals(17, new CustomHashTable<String, String>(17).capacity());
    }

    @Test
    void constructorRejectsZeroAndNegativeCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new CustomHashTable<String, String>(0));
        assertThrows(IllegalArgumentException.class, () -> new CustomHashTable<String, String>(-7));
    }

    @Test
    void rejectsNullKeysAndValues() {
        CustomHashTable<String, String> table = new CustomHashTable<>();

        assertThrows(IllegalArgumentException.class, () -> table.put(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> table.put("key", null));
        assertThrows(IllegalArgumentException.class, () -> table.get(null));
        assertThrows(IllegalArgumentException.class, () -> table.containsKey(null));
        assertThrows(IllegalArgumentException.class, () -> table.remove(null));
        assertTrue(table.isEmpty());
    }

    @Test
    void putGetAndContainsWorkForSeveralKeyTypes() {
        CustomHashTable<Object, String> table = new CustomHashTable<>();
        ControlledKey customKey = new ControlledKey("custom", 77);

        assertNull(table.put("REQ-001", "first"));
        assertNull(table.put(42, "integer"));
        assertNull(table.put(customKey, "custom-value"));

        assertEquals(3, table.size());
        assertFalse(table.isEmpty());
        assertEquals("first", table.get("REQ-001"));
        assertEquals("integer", table.get(42));
        assertEquals("custom-value", table.get(new ControlledKey("custom", 77)));
        assertTrue(table.containsKey("REQ-001"));
        assertTrue(table.containsKey(42));
        assertTrue(table.containsKey(customKey));
        assertNull(table.get("missing"));
        assertFalse(table.containsKey("missing"));
    }

    @Test
    void duplicateKeysReturnPreviousValueAndDoNotResize() {
        CustomHashTable<ControlledKey, String> table = new CustomHashTable<>(2);
        ControlledKey first = new ControlledKey("same", 5);
        ControlledKey equal = new ControlledKey("same", 99);

        assertNull(table.put(first, "original"));
        assertEquals(2, table.capacity());
        assertEquals("original", table.put(equal, "replacement"));

        assertEquals(1, table.size());
        assertEquals(2, table.capacity());
        assertEquals("replacement", table.get(first));
        assertEquals("replacement", table.get(equal));
    }

    @Test
    void duplicateUpdateNearThresholdDoesNotResizeOrChangeOtherCollisions() {
        CustomHashTable<ControlledKey, String> table = new CustomHashTable<>(5);
        ControlledKey a = new ControlledKey("a", 12);
        ControlledKey b = new ControlledKey("b", 12);
        ControlledKey c = new ControlledKey("c", 12);

        table.put(a, "A");
        table.put(b, "B");
        table.put(c, "C");
        assertEquals(5, table.capacity());
        assertEquals(0.6, table.loadFactor(), 0.000001);

        assertEquals("B", table.put(new ControlledKey("b", 12), "B2"));

        assertEquals(3, table.size());
        assertEquals(5, table.capacity());
        assertEquals("A", table.get(a));
        assertEquals("B2", table.get(b));
        assertEquals("C", table.get(c));
    }

    @Test
    void collidingUnequalKeysCoexistAndAreDistinguishedByEquals() {
        CustomHashTable<ControlledKey, String> table = new CustomHashTable<>(11);
        ControlledKey[] keys = collidingKeys("key", 6, 12345);

        for (int i = 0; i < keys.length; i++) {
            assertNull(table.put(keys[i], "v" + i));
            assertEquals(i + 1, table.size());
        }

        for (int i = 0; i < keys.length; i++) {
            assertTrue(table.containsKey(new ControlledKey("key-" + i, 12345)));
            assertEquals("v" + i, table.get(new ControlledKey("key-" + i, 12345)));
        }
        assertNull(table.get(new ControlledKey("missing", 12345)));
        assertFalse(table.containsKey(new ControlledKey("missing", 12345)));
    }

    @Test
    void collisionChainRemovalHandlesHeadMiddleAndTail() {
        CustomHashTable<ControlledKey, String> table = new CustomHashTable<>(11);
        ControlledKey first = new ControlledKey("first", 44);
        ControlledKey second = new ControlledKey("second", 44);
        ControlledKey third = new ControlledKey("third", 44);
        ControlledKey fourth = new ControlledKey("fourth", 44);

        table.put(first, "1");
        table.put(second, "2");
        table.put(third, "3");
        table.put(fourth, "4");

        assertEquals("4", table.remove(fourth));
        assertEquals("2", table.remove(second));
        assertEquals("1", table.remove(first));

        assertEquals(1, table.size());
        assertNull(table.get(fourth));
        assertNull(table.get(second));
        assertNull(table.get(first));
        assertEquals("3", table.get(third));
        assertTrue(table.containsKey(third));
    }

    @Test
    void updatingMiddleCollisionEntryPreservesChain() {
        CustomHashTable<ControlledKey, String> table = new CustomHashTable<>(11);
        ControlledKey first = new ControlledKey("first", 44);
        ControlledKey second = new ControlledKey("second", 44);
        ControlledKey third = new ControlledKey("third", 44);

        table.put(first, "1");
        table.put(second, "2");
        table.put(third, "3");

        assertEquals("2", table.put(new ControlledKey("second", 44), "two"));

        assertEquals(3, table.size());
        assertEquals("1", table.get(first));
        assertEquals("two", table.get(second));
        assertEquals("3", table.get(third));
    }

    @Test
    void negativeAndMinimumHashCodesStayReachable() {
        CustomHashTable<ControlledKey, String> table = new CustomHashTable<>(3);
        ControlledKey negative = new ControlledKey("negative", -37);
        ControlledKey minimum = new ControlledKey("minimum", Integer.MIN_VALUE);

        table.put(negative, "negative-value");
        table.put(minimum, "minimum-value");

        assertEquals("negative-value", table.get(new ControlledKey("negative", -37)));
        assertEquals("minimum-value", table.get(new ControlledKey("minimum", Integer.MIN_VALUE)));
        assertTrue(table.containsKey(minimum));
    }

    @Test
    void removeOnlyEntryAndReuseTable() {
        CustomHashTable<String, String> table = new CustomHashTable<>(5);
        table.put("one", "1");
        int capacity = table.capacity();

        assertEquals("1", table.remove("one"));

        assertTrue(table.isEmpty());
        assertEquals(0, table.size());
        assertEquals(capacity, table.capacity());
        assertNull(table.get("one"));

        table.put("two", "2");
        assertEquals(1, table.size());
        assertEquals("2", table.get("two"));
        assertEquals(capacity, table.capacity());
    }

    @Test
    void failedRemoveLeavesTableUnchanged() {
        CustomHashTable<String, String> table = new CustomHashTable<>(5);
        table.put("a", "A");
        table.put("b", "B");
        int sizeBefore = table.size();
        int capacityBefore = table.capacity();

        assertThrows(IllegalArgumentException.class, () -> table.remove("missing"));

        assertEquals(sizeBefore, table.size());
        assertEquals(capacityBefore, table.capacity());
        assertEquals("A", table.get("a"));
        assertEquals("B", table.get("b"));
    }

    @Test
    void loadFactorReflectsSizeCapacityAndThresholdBoundary() {
        CustomHashTable<Integer, String> table = new CustomHashTable<>(2);

        assertEquals(0.0, table.loadFactor());
        table.put(1, "one");
        assertEquals(2, table.capacity());
        assertEquals(0.5, table.loadFactor(), 0.000001);

        table.put(2, "two");
        assertEquals(5, table.capacity());
        assertEquals(2.0 / 5.0, table.loadFactor(), 0.000001);

        table.put(3, "three");
        assertEquals(5, table.capacity());
        assertEquals(0.6, table.loadFactor(), 0.000001);

        table.remove(1);
        assertEquals(2.0 / 5.0, table.loadFactor(), 0.000001);

        table.clear();
        assertEquals(0.0, table.loadFactor());
    }

    @Test
    void resizeTargetsNextPrimeStrictlyGreaterThanTwiceOldCapacity() {
        assertResizeCapacity(2, 5, 2);
        assertResizeCapacity(3, 7, 3);
        assertResizeCapacity(5, 11, 4);
        assertResizeCapacity(11, 23, 9);
    }

    @Test
    void resizePreservesEntriesAndStoresTriggeringEntryOnce() {
        CustomHashTable<Integer, String> table = new CustomHashTable<>(5);
        int oldCapacity = table.capacity();

        for (int i = 1; i <= 4; i++) {
            table.put(i, "v" + i);
        }

        assertEquals(11, table.capacity());
        assertTrue(table.capacity() > oldCapacity * 2);
        assertTrue(isPrime(table.capacity()));
        assertEquals(4, table.size());
        for (int i = 1; i <= 4; i++) {
            assertEquals("v" + i, table.get(i));
        }
        assertEquals("v4", table.put(4, "updated"));
        assertEquals(4, table.size());
        assertEquals("updated", table.get(4));
    }

    @Test
    void collidingEntriesRemainReachableAfterResize() {
        CustomHashTable<ControlledKey, String> table = new CustomHashTable<>(3);
        ControlledKey[] keys = collidingKeys("resize", 8, 777);

        for (int i = 0; i < keys.length; i++) {
            table.put(keys[i], "v" + i);
        }

        assertTrue(table.capacity() > 7);
        assertEquals(keys.length, table.size());
        for (int i = 0; i < keys.length; i++) {
            assertEquals("v" + i, table.get(new ControlledKey("resize-" + i, 777)));
        }
    }

    @Test
    void multipleResizesPreserveMappingsAndPrimeCapacity() {
        CustomHashTable<Integer, String> table = new CustomHashTable<>(2);

        for (int i = 1; i <= 80; i++) {
            table.put(i, "v" + i);
        }

        assertEquals(80, table.size());
        assertTrue(isPrime(table.capacity()));
        assertTrue(table.capacity() > 2);
        assertTrue(table.loadFactor() <= 0.75);
        for (int i = 1; i <= 80; i++) {
            assertEquals("v" + i, table.get(i));
        }
    }

    @Test
    void clearPreservesCapacityRemovesMappingsAndAllowsReuse() {
        CustomHashTable<Integer, String> table = new CustomHashTable<>(3);
        for (int i = 1; i <= 10; i++) {
            table.put(i, "v" + i);
        }
        int capacityAfterGrowth = table.capacity();

        table.clear();
        table.clear();

        assertTrue(table.isEmpty());
        assertEquals(0, table.size());
        assertEquals(capacityAfterGrowth, table.capacity());
        assertEquals(0.0, table.loadFactor());
        for (int i = 1; i <= 10; i++) {
            assertNull(table.get(i));
        }

        for (int i = 11; i <= 40; i++) {
            table.put(i, "v" + i);
        }

        assertEquals(30, table.size());
        assertTrue(table.capacity() >= capacityAfterGrowth);
        for (int i = 11; i <= 40; i++) {
            assertEquals("v" + i, table.get(i));
        }
    }

    @Test
    void storesUpdatesAndRemovesDeliveryRequestsByRequestId() {
        CustomHashTable<String, DeliveryRequest> table = new CustomHashTable<>(3);
        DeliveryRequest first = request("REQ-001", 1);
        DeliveryRequest second = request("REQ-002", 2);
        DeliveryRequest third = request("REQ-003", 3);
        DeliveryRequest replacement = request("REQ-002", 20);

        table.put(first.getRequestId(), first);
        table.put(second.getRequestId(), second);
        table.put(third.getRequestId(), third);

        assertSame(first, table.get("REQ-001"));
        assertSame(second, table.get("REQ-002"));
        assertSame(second, table.put("REQ-002", replacement));
        assertSame(replacement, table.get("REQ-002"));

        DeliveryRequest removed = table.remove("REQ-002");
        assertSame(replacement, removed);
        assertEquals("REQ-002", removed.getRequestId());
        assertNull(table.get("REQ-002"));
        assertSame(first, table.get("REQ-001"));
        assertSame(third, table.get("REQ-003"));
    }

    @Test
    void multipleDeliveryRequestsSurviveResizing() {
        CustomHashTable<String, DeliveryRequest> table = new CustomHashTable<>(2);
        DeliveryRequest[] requests = new DeliveryRequest[24];

        for (int i = 0; i < requests.length; i++) {
            String requestId = requestId(i + 1);
            requests[i] = request(requestId, i + 1);
            table.put(requestId, requests[i]);
        }

        assertEquals(requests.length, table.size());
        assertTrue(table.capacity() > 23);
        for (int i = 0; i < requests.length; i++) {
            assertSame(requests[i], table.get(requestId(i + 1)));
        }
    }

    @Test
    void largerDeterministicSequenceMaintainsAccurateState() {
        CustomHashTable<Integer, String> table = new CustomHashTable<>(2);

        for (int i = 1; i <= 100; i++) {
            table.put(i, "v" + i);
        }
        assertEquals(100, table.size());
        assertTrue(isPrime(table.capacity()));

        for (int i = 10; i <= 100; i += 10) {
            assertEquals("v" + i, table.put(i, "updated-" + i));
        }
        assertEquals(100, table.size());

        for (int i = 10; i <= 100; i += 10) {
            assertEquals("updated-" + i, table.get(i));
        }
        for (int i = 1; i <= 25; i += 2) {
            assertEquals(i % 10 == 0 ? "updated-" + i : "v" + i, table.remove(i));
        }

        assertEquals(87, table.size());
        for (int i = 1; i <= 25; i += 2) {
            assertFalse(table.containsKey(i));
            assertNull(table.get(i));
        }
        for (int i = 2; i <= 100; i += 2) {
            assertTrue(table.containsKey(i));
        }
        assertTrue(isPrime(table.capacity()));
    }

    private void assertResizeCapacity(int requestedCapacity, int expectedCapacity, int insertionCount) {
        CustomHashTable<Integer, String> table = new CustomHashTable<>(requestedCapacity);
        int oldCapacity = table.capacity();

        for (int i = 1; i <= insertionCount; i++) {
            table.put(i, "v" + i);
        }

        assertEquals(expectedCapacity, table.capacity());
        assertTrue(table.capacity() > oldCapacity * 2);
        assertTrue(isPrime(table.capacity()));
        assertEquals(insertionCount, table.size());
        for (int i = 1; i <= insertionCount; i++) {
            assertEquals("v" + i, table.get(i));
        }
    }

    private ControlledKey[] collidingKeys(String prefix, int count, int hash) {
        ControlledKey[] keys = new ControlledKey[count];
        for (int i = 0; i < count; i++) {
            keys[i] = new ControlledKey(prefix + "-" + i, hash);
        }
        return keys;
    }

    private DeliveryRequest request(String requestId, int index) {
        LocalDateTime submitted = LocalDateTime.of(2026, 1, 1, 10, 0).plusMinutes(index);
        return new DeliveryRequest(
                requestId,
                "LOC-" + index,
                "LOC-" + (index + 1),
                "GROCERY",
                (index % 5) + 1,
                1.5 + index,
                submitted,
                submitted.plusHours(2),
                RequestStatus.PENDING,
                5.0 + index
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
