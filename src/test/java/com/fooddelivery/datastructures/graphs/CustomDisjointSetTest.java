package com.fooddelivery.datastructures.graphs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomDisjointSetTest {
    @Test
    void constructionCapacityValidationAndEmptyClearAreSafe() {
        CustomDisjointSet<String> disjointSet = new CustomDisjointSet<>();
        CustomDisjointSet<String> customCapacity = new CustomDisjointSet<>(4);

        assertTrue(disjointSet.isEmpty());
        assertEquals(0, disjointSet.size());
        assertEquals(0, disjointSet.setCount());
        assertTrue(disjointSet.isValidDisjointSet());
        assertTrue(customCapacity.isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new CustomDisjointSet<String>(0));
        assertThrows(IllegalArgumentException.class, () -> new CustomDisjointSet<String>(-3));

        disjointSet.clear();

        assertTrue(disjointSet.isEmpty());
        assertEquals(0, disjointSet.size());
        assertEquals(0, disjointSet.setCount());
        assertTrue(disjointSet.isValidDisjointSet());
    }

    @Test
    void makeSetCreatesSeparateSingletonsAndRejectsDuplicates() {
        CustomDisjointSet<ControlledKey> disjointSet = new CustomDisjointSet<>();
        ControlledKey a = new ControlledKey("A", 7);
        ControlledKey equalA = new ControlledKey("A", 7);
        ControlledKey b = new ControlledKey("B", 7);
        disjointSet.makeSet(a);
        disjointSet.makeSet(b);

        assertEquals(2, disjointSet.size());
        assertEquals(2, disjointSet.setCount());
        assertTrue(disjointSet.contains(a));
        assertTrue(disjointSet.contains(equalA));
        assertSame(a, disjointSet.find(equalA));
        assertSame(a, disjointSet.parentOf(a));
        assertEquals(0, disjointSet.depthOf(a));
        assertEquals(0, disjointSet.rankOf(a));
        assertFalse(disjointSet.connected(a, b));
        assertTrue(disjointSet.isValidDisjointSet());

        assertThrows(IllegalArgumentException.class, () -> disjointSet.makeSet(null));
        assertThrows(IllegalArgumentException.class, () -> disjointSet.makeSet(equalA));
        assertEquals(2, disjointSet.size());
        assertEquals(2, disjointSet.setCount());
        assertFalse(disjointSet.connected(a, b));
    }

    @Test
    void publicOperationsRejectNullAndMissingValues() {
        CustomDisjointSet<String> disjointSet = new CustomDisjointSet<>();
        disjointSet.makeSet("A");

        assertThrows(IllegalArgumentException.class, () -> disjointSet.find(null));
        assertThrows(IllegalArgumentException.class, () -> disjointSet.find("missing"));
        assertThrows(IllegalArgumentException.class, () -> disjointSet.union(null, "A"));
        assertThrows(IllegalArgumentException.class, () -> disjointSet.union("A", null));
        assertThrows(IllegalArgumentException.class, () -> disjointSet.union("A", "missing"));
        assertThrows(IllegalArgumentException.class, () -> disjointSet.connected(null, "A"));
        assertThrows(IllegalArgumentException.class, () -> disjointSet.connected("A", "missing"));
        assertThrows(IllegalArgumentException.class, () -> disjointSet.contains(null));
        assertThrows(IllegalArgumentException.class, () -> disjointSet.parentOf(null));
        assertThrows(IllegalArgumentException.class, () -> disjointSet.parentOf("missing"));
        assertThrows(IllegalArgumentException.class, () -> disjointSet.rankOf(null));
        assertThrows(IllegalArgumentException.class, () -> disjointSet.rankOf("missing"));
        assertThrows(IllegalArgumentException.class, () -> disjointSet.depthOf(null));
        assertThrows(IllegalArgumentException.class, () -> disjointSet.depthOf("missing"));
        assertEquals(1, disjointSet.size());
        assertEquals(1, disjointSet.setCount());
        assertTrue(disjointSet.isValidDisjointSet());
    }

    @Test
    void equalRankUnionUsesFirstRepresentativeAndRepeatedUnionIsStable() {
        CustomDisjointSet<String> disjointSet = new CustomDisjointSet<>();
        disjointSet.makeSet("A");
        disjointSet.makeSet("B");
        disjointSet.makeSet("C");

        assertTrue(disjointSet.union("A", "B"));

        assertEquals(3, disjointSet.size());
        assertEquals(2, disjointSet.setCount());
        assertTrue(disjointSet.connected("A", "B"));
        assertTrue(disjointSet.connected("B", "A"));
        assertTrue(disjointSet.connected("A", "A"));
        assertSame("A", disjointSet.find("B"));
        assertSame("A", disjointSet.parentOf("B"));
        assertEquals(1, disjointSet.rankOf("A"));
        assertEquals(0, disjointSet.rankOf("B"));
        assertEquals(0, disjointSet.rankOf("C"));
        assertFalse(disjointSet.connected("A", "C"));

        assertFalse(disjointSet.union("B", "A"));
        assertFalse(disjointSet.union("A", "A"));
        assertEquals(2, disjointSet.setCount());
        assertEquals(1, disjointSet.rankOf("A"));
        assertEquals(0, disjointSet.rankOf("B"));
        assertTrue(disjointSet.isValidDisjointSet());
    }

    @Test
    void unionByRankKeepsHigherRankRepresentativeAcrossArgumentOrders() {
        CustomDisjointSet<String> disjointSet = new CustomDisjointSet<>();
        makeSets(disjointSet, "A", "B", "C", "D", "E");
        disjointSet.union("A", "B");
        disjointSet.union("C", "D");
        disjointSet.union("A", "C");

        assertEquals(2, disjointSet.rankOf("A"));
        assertTrue(disjointSet.union("E", "D"));

        assertSame("A", disjointSet.find("E"));
        assertSame("A", disjointSet.parentOf("E"));
        assertEquals(2, disjointSet.rankOf("A"));
        assertEquals(1, disjointSet.setCount());
        assertTrue(disjointSet.connected("B", "E"));
        assertTrue(disjointSet.connected("D", "A"));
        assertTrue(disjointSet.isValidDisjointSet());
    }

    @Test
    void nonRootArgumentsResolveToCurrentRootsBeforeEqualRankUnion() {
        CustomDisjointSet<String> disjointSet = new CustomDisjointSet<>();
        makeSets(disjointSet, "A", "B", "C", "D");
        disjointSet.union("A", "B");
        disjointSet.union("C", "D");

        assertTrue(disjointSet.union("B", "D"));

        assertSame("A", disjointSet.find("D"));
        assertSame("A", disjointSet.parentOf("C"));
        assertEquals(2, disjointSet.rankOf("A"));
        assertEquals(1, disjointSet.setCount());
        assertTrue(disjointSet.isValidDisjointSet());
    }

    @Test
    void findPerformsFullPathCompressionWithoutChangingCountsOrRootRank() {
        CustomDisjointSet<String> disjointSet = new CustomDisjointSet<>();
        makeSets(disjointSet, "A", "B", "C", "D");
        disjointSet.union("A", "B");
        disjointSet.union("C", "D");
        disjointSet.union("A", "C");

        assertEquals(2, disjointSet.depthOf("D"));
        assertSame("C", disjointSet.parentOf("D"));
        int size = disjointSet.size();
        int setCount = disjointSet.setCount();
        int rootRank = disjointSet.rankOf("A");

        assertSame("A", disjointSet.find("D"));

        assertEquals(size, disjointSet.size());
        assertEquals(setCount, disjointSet.setCount());
        assertEquals(rootRank, disjointSet.rankOf("A"));
        assertSame("A", disjointSet.parentOf("D"));
        assertEquals(1, disjointSet.depthOf("D"));
        assertEquals(0, disjointSet.depthOf("A"));
        assertTrue(disjointSet.isValidDisjointSet());
    }

    @Test
    void connectedAlsoCompressesPathsAndPreservesCounts() {
        CustomDisjointSet<String> disjointSet = new CustomDisjointSet<>();
        makeSets(disjointSet, "A", "B", "C", "D");
        disjointSet.union("A", "B");
        disjointSet.union("C", "D");
        disjointSet.union("A", "C");
        int size = disjointSet.size();
        int setCount = disjointSet.setCount();

        assertTrue(disjointSet.connected("D", "B"));

        assertEquals(size, disjointSet.size());
        assertEquals(setCount, disjointSet.setCount());
        assertSame("A", disjointSet.parentOf("D"));
        assertEquals(1, disjointSet.depthOf("D"));
        assertTrue(disjointSet.isValidDisjointSet());
    }

    @Test
    void diagnosticsDoNotMutateStructureAndClearAllowsReuse() {
        CustomDisjointSet<String> disjointSet = new CustomDisjointSet<>();
        makeSets(disjointSet, "A", "B", "C", "D");
        disjointSet.union("A", "B");
        disjointSet.union("C", "D");
        disjointSet.union("A", "C");
        assertEquals(2, disjointSet.depthOf("D"));

        assertSame("C", disjointSet.parentOf("D"));
        assertEquals(1, disjointSet.rankOf("C"));
        assertEquals(2, disjointSet.depthOf("D"));
        assertTrue(disjointSet.isValidDisjointSet());

        disjointSet.clear();

        assertTrue(disjointSet.isEmpty());
        assertEquals(0, disjointSet.size());
        assertEquals(0, disjointSet.setCount());
        assertFalse(disjointSet.contains("A"));
        assertThrows(IllegalArgumentException.class, () -> disjointSet.find("A"));
        assertTrue(disjointSet.isValidDisjointSet());

        makeSets(disjointSet, "X", "Y", "Z");
        assertTrue(disjointSet.union("X", "Y"));
        assertTrue(disjointSet.connected("Y", "X"));
        assertFalse(disjointSet.connected("X", "Z"));
        assertEquals(2, disjointSet.setCount());
        assertTrue(disjointSet.isValidDisjointSet());
    }

    @Test
    void integerStringEqualObjectsAndHashCollisionsWork() {
        CustomDisjointSet<Integer> integers = new CustomDisjointSet<>(4);
        for (int i = 1; i <= 30; i++) {
            integers.makeSet(i);
        }
        integers.union(1, 2);
        integers.union(3, 4);
        integers.union(2, 4);
        assertEquals(30, integers.size());
        assertEquals(27, integers.setCount());
        assertTrue(integers.connected(1, 4));
        assertFalse(integers.connected(1, 5));
        assertTrue(integers.isValidDisjointSet());

        CustomDisjointSet<String> strings = new CustomDisjointSet<>();
        makeSets(strings, "REST-1", "HOME-1");
        strings.union("REST-1", "HOME-1");
        assertTrue(strings.connected("HOME-1", "REST-1"));

        CustomDisjointSet<ControlledKey> keys = new CustomDisjointSet<>(2);
        ControlledKey a = new ControlledKey("A", 12);
        ControlledKey equalA = new ControlledKey("A", 12);
        ControlledKey b = new ControlledKey("B", 12);
        ControlledKey c = new ControlledKey("C", 12);
        keys.makeSet(a);
        keys.makeSet(b);
        keys.makeSet(c);
        assertTrue(keys.contains(equalA));
        assertThrows(IllegalArgumentException.class, () -> keys.makeSet(equalA));
        assertTrue(keys.union(equalA, b));
        assertTrue(keys.union(c, equalA));
        assertSame(a, keys.find(c));
        assertTrue(keys.isValidDisjointSet());
    }

    @Test
    void foodDeliveryLocationIdsSupportCycleDetectionDecisions() {
        CustomDisjointSet<String> disjointSet = new CustomDisjointSet<>();
        makeSets(disjointSet, "REST-1", "JUNC-1", "HOME-1", "CAMP-1", "SHOP-1");

        assertEquals(5, disjointSet.setCount());
        assertFalse(disjointSet.connected("REST-1", "HOME-1"));
        assertTrue(disjointSet.union("REST-1", "JUNC-1"));
        assertTrue(disjointSet.union("JUNC-1", "HOME-1"));
        assertTrue(disjointSet.connected("REST-1", "HOME-1"));

        boolean wouldCreateCycle = disjointSet.connected("REST-1", "HOME-1");
        assertTrue(wouldCreateCycle);
        boolean canJoinSeparateComponents = !disjointSet.connected("CAMP-1", "SHOP-1");
        assertTrue(canJoinSeparateComponents);
        assertTrue(disjointSet.union("CAMP-1", "SHOP-1"));

        int accepted = 0;
        accepted += acceptIfSeparate(disjointSet, "HOME-1", "CAMP-1");
        accepted += acceptIfSeparate(disjointSet, "REST-1", "HOME-1");
        accepted += acceptIfSeparate(disjointSet, "SHOP-1", "JUNC-1");

        assertEquals(1, accepted);
        assertEquals(1, disjointSet.setCount());
        assertTrue(disjointSet.connected("REST-1", "SHOP-1"));
        assertTrue(disjointSet.isValidDisjointSet());
    }

    @Test
    void largerDeterministicSequenceSurvivesResizingCompressionAndReuse() {
        CustomDisjointSet<String> disjointSet = new CustomDisjointSet<>(2);
        for (int i = 1; i <= 100; i++) {
            disjointSet.makeSet(locationId(i));
        }

        assertEquals(100, disjointSet.size());
        assertEquals(100, disjointSet.setCount());
        for (int group = 0; group < 10; group++) {
            String root = locationId(group * 10 + 1);
            for (int offset = 2; offset <= 10; offset++) {
                assertTrue(disjointSet.union(root, locationId(group * 10 + offset)));
            }
        }

        assertEquals(10, disjointSet.setCount());
        assertTrue(disjointSet.connected(locationId(1), locationId(10)));
        assertTrue(disjointSet.connected(locationId(51), locationId(60)));
        assertFalse(disjointSet.connected(locationId(1), locationId(11)));
        assertTrue(disjointSet.union(locationId(1), locationId(11)));
        assertTrue(disjointSet.union(locationId(21), locationId(31)));
        assertTrue(disjointSet.union(locationId(11), locationId(31)));
        assertTrue(disjointSet.connected(locationId(1), locationId(40)));
        assertFalse(disjointSet.connected(locationId(1), locationId(50)));

        for (int i = 1; i <= 100; i++) {
            disjointSet.find(locationId(i));
        }

        assertEquals(100, disjointSet.size());
        assertEquals(7, disjointSet.setCount());
        assertTrue(disjointSet.isValidDisjointSet());

        disjointSet.clear();
        makeSets(disjointSet, "LOC-101", "LOC-102");
        assertTrue(disjointSet.union("LOC-101", "LOC-102"));
        assertTrue(disjointSet.connected("LOC-102", "LOC-101"));
    }

    private void makeSets(CustomDisjointSet<String> disjointSet, String... values) {
        for (String value : values) {
            disjointSet.makeSet(value);
        }
    }

    private int acceptIfSeparate(CustomDisjointSet<String> disjointSet, String first, String second) {
        if (disjointSet.connected(first, second)) {
            return 0;
        }
        disjointSet.union(first, second);
        return 1;
    }

    private String locationId(int number) {
        if (number < 10) {
            return "LOC-00" + number;
        }
        if (number < 100) {
            return "LOC-0" + number;
        }
        return "LOC-" + number;
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
