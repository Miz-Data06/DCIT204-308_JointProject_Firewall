package com.fooddelivery.datastructures.hashing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomSetTest {
    @Test
    void defaultSetStartsEmptyWithHashTableCapacity() {
        CustomSet<String> set = new CustomSet<>();

        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
        assertEquals(11, set.capacity());
        assertEquals(0.0, set.loadFactor());
        assertFalse(set.contains("missing"));
    }

    @Test
    void constructorDelegatesCapacityValidationAndNormalization() {
        assertEquals(2, new CustomSet<String>(1).capacity());
        assertEquals(5, new CustomSet<String>(4).capacity());
        assertEquals(17, new CustomSet<String>(17).capacity());
        assertThrows(IllegalArgumentException.class, () -> new CustomSet<String>(0));
        assertThrows(IllegalArgumentException.class, () -> new CustomSet<String>(-4));
    }

    @Test
    void rejectsNullValues() {
        CustomSet<String> set = new CustomSet<>();

        assertThrows(IllegalArgumentException.class, () -> set.add(null));
        assertThrows(IllegalArgumentException.class, () -> set.contains(null));
        assertThrows(IllegalArgumentException.class, () -> set.remove(null));
        assertTrue(set.isEmpty());
    }

    @Test
    void addContainsAndDuplicateBehaviourWork() {
        CustomSet<String> set = new CustomSet<>();

        assertTrue(set.add("LOC-001"));
        assertFalse(set.isEmpty());
        assertEquals(1, set.size());
        assertTrue(set.contains("LOC-001"));
        assertFalse(set.contains("LOC-999"));

        int capacity = set.capacity();
        assertFalse(set.add("LOC-001"));
        assertEquals(1, set.size());
        assertEquals(capacity, set.capacity());
    }

    @Test
    void equalDistinctObjectsRepresentOneElement() {
        CustomSet<ControlledValue> set = new CustomSet<>(2);
        ControlledValue first = new ControlledValue("same", 11);
        ControlledValue equal = new ControlledValue("same", 11);

        assertTrue(set.add(first));
        assertFalse(set.add(equal));

        assertEquals(1, set.size());
        assertTrue(set.contains(first));
        assertTrue(set.contains(equal));
        assertEquals(2, set.capacity());
    }

    @Test
    void collidingValuesCoexistAndRemainDetectable() {
        CustomSet<ControlledValue> set = new CustomSet<>(11);
        ControlledValue a = new ControlledValue("a", 42);
        ControlledValue b = new ControlledValue("b", 42);
        ControlledValue c = new ControlledValue("c", 42);

        assertTrue(set.add(a));
        assertTrue(set.add(b));
        assertTrue(set.add(c));

        assertEquals(3, set.size());
        assertTrue(set.contains(new ControlledValue("a", 42)));
        assertTrue(set.contains(new ControlledValue("b", 42)));
        assertTrue(set.contains(new ControlledValue("c", 42)));
        assertFalse(set.contains(new ControlledValue("missing", 42)));
    }

    @Test
    void removingOneCollisionPreservesOthers() {
        CustomSet<ControlledValue> set = new CustomSet<>(11);
        ControlledValue a = new ControlledValue("a", 88);
        ControlledValue b = new ControlledValue("b", 88);
        ControlledValue c = new ControlledValue("c", 88);
        set.add(a);
        set.add(b);
        set.add(c);
        int capacity = set.capacity();

        assertTrue(set.remove(new ControlledValue("b", 88)));

        assertEquals(2, set.size());
        assertEquals(capacity, set.capacity());
        assertTrue(set.contains(a));
        assertFalse(set.contains(b));
        assertTrue(set.contains(c));
    }

    @Test
    void missingRemoveThrowsAndPreservesState() {
        CustomSet<String> set = new CustomSet<>(5);
        set.add("AREA-A");
        set.add("AREA-B");
        int size = set.size();
        int capacity = set.capacity();

        assertThrows(IllegalArgumentException.class, () -> set.remove("AREA-Z"));

        assertEquals(size, set.size());
        assertEquals(capacity, set.capacity());
        assertTrue(set.contains("AREA-A"));
        assertTrue(set.contains("AREA-B"));
    }

    @Test
    void negativeAndMinimumHashCodesWork() {
        CustomSet<ControlledValue> set = new CustomSet<>(3);
        ControlledValue negative = new ControlledValue("negative", -31);
        ControlledValue minimum = new ControlledValue("minimum", Integer.MIN_VALUE);

        assertTrue(set.add(negative));
        assertTrue(set.add(minimum));

        assertTrue(set.contains(new ControlledValue("negative", -31)));
        assertTrue(set.contains(new ControlledValue("minimum", Integer.MIN_VALUE)));
    }

    @Test
    void loadFactorAndResizingAreInheritedFromHashTable() {
        CustomSet<Integer> set = new CustomSet<>(2);

        assertTrue(set.add(1));
        assertEquals(2, set.capacity());
        assertEquals(0.5, set.loadFactor(), 0.000001);

        assertTrue(set.add(2));
        assertEquals(5, set.capacity());
        assertTrue(set.capacity() > 2 * 2);
        assertTrue(isPrime(set.capacity()));
        assertEquals(2.0 / 5.0, set.loadFactor(), 0.000001);
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
    }

    @Test
    void duplicateAdditionNearThresholdDoesNotResize() {
        CustomSet<Integer> set = new CustomSet<>(5);
        set.add(1);
        set.add(2);
        set.add(3);
        int capacity = set.capacity();

        assertFalse(set.add(2));

        assertEquals(3, set.size());
        assertEquals(capacity, set.capacity());
        assertEquals(0.6, set.loadFactor(), 0.000001);
    }

    @Test
    void clearEmptiesSetPreservesCapacityAndAllowsReuse() {
        CustomSet<Integer> set = new CustomSet<>(3);
        for (int i = 1; i <= 12; i++) {
            set.add(i);
        }
        int capacity = set.capacity();

        set.clear();
        set.clear();

        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
        assertEquals(capacity, set.capacity());
        assertEquals(0.0, set.loadFactor());
        assertFalse(set.contains(1));

        assertTrue(set.add(20));
        assertTrue(set.contains(20));
        assertEquals(1, set.size());
    }

    @Test
    void locationIdsAndServiceAreasRemainUnique() {
        CustomSet<String> locationIds = new CustomSet<>(3);
        CustomSet<String> serviceAreas = new CustomSet<>(3);

        assertTrue(locationIds.add("LOC-001"));
        assertTrue(locationIds.add("LOC-002"));
        assertFalse(locationIds.add("LOC-001"));
        assertTrue(serviceAreas.add("North"));
        assertTrue(serviceAreas.add("Central"));
        assertFalse(serviceAreas.add("North"));

        assertEquals(2, locationIds.size());
        assertEquals(2, serviceAreas.size());
        assertTrue(locationIds.contains("LOC-002"));
        assertTrue(serviceAreas.remove("Central"));
        assertFalse(serviceAreas.contains("Central"));
    }

    @Test
    void valuesSurviveResizing() {
        CustomSet<String> set = new CustomSet<>(2);
        String[] values = new String[24];

        for (int i = 0; i < values.length; i++) {
            values[i] = "LOC-" + (i + 1);
            assertTrue(set.add(values[i]));
        }

        assertEquals(values.length, set.size());
        assertTrue(set.capacity() > 23);
        assertTrue(isPrime(set.capacity()));
        for (String value : values) {
            assertTrue(set.contains(value));
        }
    }

    @Test
    void largerDeterministicSequenceRemainsCorrect() {
        CustomSet<Integer> set = new CustomSet<>(2);

        for (int i = 1; i <= 80; i++) {
            assertTrue(set.add(i));
        }
        for (int i = 10; i <= 80; i += 10) {
            assertFalse(set.add(i));
        }
        for (int i = 1; i <= 25; i += 2) {
            assertTrue(set.remove(i));
        }

        assertEquals(67, set.size());
        assertTrue(isPrime(set.capacity()));
        for (int i = 1; i <= 25; i += 2) {
            assertFalse(set.contains(i));
        }
        for (int i = 2; i <= 80; i += 2) {
            assertTrue(set.contains(i));
        }
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

    private static final class ControlledValue {
        private final String id;
        private final int hash;

        private ControlledValue(String id, int hash) {
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
            if (!(other instanceof ControlledValue that)) {
                return false;
            }
            return id.equals(that.id);
        }
    }
}
