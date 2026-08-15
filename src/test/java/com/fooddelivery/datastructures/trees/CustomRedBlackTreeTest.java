package com.fooddelivery.datastructures.trees;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.RequestStatus;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomRedBlackTreeTest {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 3, 12, 10, 0);

    @Test
    void newTreeHasEmptyStateAndValidEmptyTraversals() {
        CustomRedBlackTree<String, String> tree = new CustomRedBlackTree<>();

        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        assertEquals(-1, tree.height());
        assertTrue(tree.isValidRedBlackTree());
        assertNull(tree.search("M"));
        assertFalse(tree.containsKey("M"));
        assertEquals(0, tree.inorderTraversal().size());
        assertEquals(0, tree.preorderTraversal().size());
        assertEquals(0, tree.postorderTraversal().size());
    }

    @Test
    void nullKeysAndValuesThrowIllegalArgumentException() {
        CustomRedBlackTree<String, String> tree = new CustomRedBlackTree<>();

        assertThrows(IllegalArgumentException.class, () -> tree.insert(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> tree.insert("key", null));
        assertThrows(IllegalArgumentException.class, () -> tree.search(null));
        assertThrows(IllegalArgumentException.class, () -> tree.containsKey(null));
    }

    @Test
    void firstInsertionCreatesBlackRootEquivalentAndTraversesCorrectly() {
        CustomRedBlackTree<String, String> tree = new CustomRedBlackTree<>();

        tree.insert("M", "middle");

        assertFalse(tree.isEmpty());
        assertEquals(1, tree.size());
        assertEquals(0, tree.height());
        assertEquals("middle", tree.search("M"));
        assertTrue(tree.containsKey("M"));
        assertArrayValues(tree.inorderTraversal(), "middle");
        assertArrayValues(tree.preorderTraversal(), "middle");
        assertArrayValues(tree.postorderTraversal(), "middle");
        assertTrue(tree.isValidRedBlackTree());
    }

    @Test
    void searchFindsRootBothSidesAndDeeperDescendants() {
        CustomRedBlackTree<String, String> tree = populatedLetterTree();

        assertEquals("middle", tree.search("M"));
        assertEquals("alpha", tree.search("A"));
        assertEquals("zulu", tree.search("Z"));
        assertEquals("charlie", tree.search("C"));
        assertNull(tree.search("0"));
        assertNull(tree.search("ZZ"));
        assertTrue(tree.containsKey("R"));
        assertFalse(tree.containsKey("Q"));
        assertTrue(tree.isValidRedBlackTree());
    }

    @Test
    void integerAndCustomComparableKeysWork() {
        CustomRedBlackTree<Integer, String> integerTree = new CustomRedBlackTree<>();
        integerTree.insert(5, "five");
        integerTree.insert(2, "two");
        integerTree.insert(8, "eight");
        assertEquals("two", integerTree.search(2));
        assertArrayValues(integerTree.inorderTraversal(), "two", "five", "eight");
        assertTrue(integerTree.isValidRedBlackTree());

        CustomRedBlackTree<RankKey, String> customTree = new CustomRedBlackTree<>();
        customTree.insert(new RankKey("normal", 3), "normal");
        customTree.insert(new RankKey("urgent", 1), "urgent");
        customTree.insert(new RankKey("soon", 2), "soon");
        assertEquals("urgent", customTree.search(new RankKey("same-rank", 1)));
        assertArrayValues(customTree.inorderTraversal(), "urgent", "soon", "normal");
        assertTrue(customTree.isValidRedBlackTree());
    }

    @Test
    void duplicateKeyReplacesValueWithoutChangingSizeHeightOrValidity() {
        CustomRedBlackTree<String, String> tree = populatedLetterTree();
        int sizeBeforeUpdate = tree.size();
        int heightBeforeUpdate = tree.height();

        tree.insert("M", "replacement-root");
        tree.insert("A", "replacement-leaf");

        assertEquals(sizeBeforeUpdate, tree.size());
        assertEquals(heightBeforeUpdate, tree.height());
        assertEquals("replacement-root", tree.search("M"));
        assertEquals("replacement-leaf", tree.search("A"));
        assertEquals("charlie", tree.search("C"));
        assertArrayValues(tree.inorderTraversal(),
                "replacement-leaf", "charlie", "echo", "replacement-root", "romeo", "tango", "zulu");
        assertTrue(tree.isValidRedBlackTree());
    }

    @Test
    void compareToEqualDifferentKeyObjectsUpdateSameEntry() {
        CustomRedBlackTree<RankKey, String> tree = new CustomRedBlackTree<>();

        tree.insert(new RankKey("first", 7), "first-value");
        tree.insert(new RankKey("second", 7), "replacement-value");

        assertEquals(1, tree.size());
        assertEquals("replacement-value", tree.search(new RankKey("third", 7)));
        assertTrue(tree.isValidRedBlackTree());
    }

    @Test
    void rotationAndRecolouringInsertionCasesRemainValidAndSorted() {
        assertValidSortedTree(new int[] {30, 20, 10}, "10", "20", "30");
        assertValidSortedTree(new int[] {10, 20, 30}, "10", "20", "30");
        assertValidSortedTree(new int[] {30, 10, 20}, "10", "20", "30");
        assertValidSortedTree(new int[] {10, 30, 20}, "10", "20", "30");
        assertValidSortedTree(new int[] {10, 5, 15, 1}, "1", "5", "10", "15");
        assertValidSortedTree(new int[] {10, 5, 15, 1, 0}, "0", "1", "5", "10", "15");
        assertValidSortedTree(new int[] {10, 20, 5, 30, 25}, "5", "10", "20", "25", "30");
    }

    @Test
    void everyIncrementalInsertionInMixedSequencePreservesValidity() {
        CustomRedBlackTree<Integer, String> tree = new CustomRedBlackTree<>();
        int[] keys = {40, 20, 60, 10, 30, 50, 70, 25, 35, 45, 55, 65, 75, 5, 1};

        int expectedSize = 0;
        for (int key : keys) {
            tree.insert(key, Integer.toString(key));
            expectedSize++;
            assertEquals(expectedSize, tree.size());
            assertTrue(tree.isValidRedBlackTree());
        }

        for (int key : keys) {
            assertEquals(Integer.toString(key), tree.search(key));
        }
        assertTrue(tree.height() <= redBlackHeightBound(tree.size()));
    }

    @Test
    void traversalsReturnCustomDynamicArraySnapshotsAndReflectUpdates() {
        CustomRedBlackTree<String, String> tree = populatedLetterTree();
        CustomDynamicArray<String> inorder = tree.inorderTraversal();
        CustomDynamicArray<String> preorder = tree.preorderTraversal();
        CustomDynamicArray<String> postorder = tree.postorderTraversal();

        assertInstanceOf(CustomDynamicArray.class, inorder);
        assertEquals(tree.size(), inorder.size());
        assertEquals(tree.size(), preorder.size());
        assertEquals(tree.size(), postorder.size());
        assertArrayValues(inorder, "alpha", "charlie", "echo", "middle", "romeo", "tango", "zulu");
        assertContainsEachOnce(preorder, "alpha", "charlie", "echo", "middle", "romeo", "tango", "zulu");
        assertContainsEachOnce(postorder, "alpha", "charlie", "echo", "middle", "romeo", "tango", "zulu");

        inorder.set(0, "changed-snapshot");
        tree.insert("B", "bravo");
        tree.insert("C", "updated-charlie");

        assertEquals("alpha", tree.search("A"));
        assertEquals("updated-charlie", tree.search("C"));
        assertEquals("changed-snapshot", inorder.get(0));
        assertEquals(7, inorder.size());
        assertArrayValues(tree.inorderTraversal(),
                "alpha", "bravo", "updated-charlie", "echo", "middle", "romeo", "tango", "zulu");
        assertTrue(tree.isValidRedBlackTree());
    }

    @Test
    void ascendingAndDescendingInsertionsStayBalancedWithinBound() {
        CustomRedBlackTree<Integer, String> ascending = new CustomRedBlackTree<>();
        CustomRedBlackTree<Integer, String> descending = new CustomRedBlackTree<>();

        for (int key = 1; key <= 20; key++) {
            ascending.insert(key, Integer.toString(key));
            assertTrue(ascending.isValidRedBlackTree());
        }
        for (int key = 20; key >= 1; key--) {
            descending.insert(key, Integer.toString(key));
            assertTrue(descending.isValidRedBlackTree());
        }

        assertTrue(ascending.height() < 19);
        assertTrue(descending.height() < 19);
        assertTrue(ascending.height() <= redBlackHeightBound(ascending.size()));
        assertTrue(descending.height() <= redBlackHeightBound(descending.size()));
    }

    @Test
    void redBlackHeightIsLowerThanSkewedCustomBstForSameAscendingSequence() {
        CustomRedBlackTree<Integer, String> redBlackTree = new CustomRedBlackTree<>();
        CustomBST<Integer, String> bst = new CustomBST<>();

        for (int key = 1; key <= 30; key++) {
            redBlackTree.insert(key, Integer.toString(key));
            bst.insert(key, Integer.toString(key));
        }

        assertTrue(redBlackTree.isValidRedBlackTree());
        assertTrue(redBlackTree.height() <= redBlackHeightBound(redBlackTree.size()));
        assertTrue(redBlackTree.height() < bst.height());
    }

    @Test
    void clearEmptiesTreeAndAllowsValidReuse() {
        CustomRedBlackTree<String, String> tree = populatedLetterTree();

        tree.clear();

        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        assertEquals(-1, tree.height());
        assertTrue(tree.isValidRedBlackTree());
        assertNull(tree.search("M"));
        assertEquals(0, tree.inorderTraversal().size());
        assertEquals(0, tree.preorderTraversal().size());
        assertEquals(0, tree.postorderTraversal().size());
        tree.clear();

        tree.insert("N", "new");

        assertEquals("new", tree.search("N"));
        assertEquals(1, tree.size());
        assertTrue(tree.isValidRedBlackTree());
    }

    @Test
    void deliveryRequestsAreIndexedByRequestIdWithoutMutation() {
        CustomRedBlackTree<String, DeliveryRequest> tree = new CustomRedBlackTree<>();
        DeliveryRequest sr010 = request("SR010", 10.0);
        DeliveryRequest sr002 = request("SR002", 20.0);
        DeliveryRequest sr001 = request("SR001", 30.0);
        DeliveryRequest replacement = request("SR002", 40.0);

        tree.insert(sr010.getRequestId(), sr010);
        assertTrue(tree.isValidRedBlackTree());
        tree.insert(sr002.getRequestId(), sr002);
        assertTrue(tree.isValidRedBlackTree());
        tree.insert(sr001.getRequestId(), sr001);
        assertTrue(tree.isValidRedBlackTree());

        assertSame(sr002, tree.search("SR002"));
        tree.insert("SR002", replacement);

        CustomDynamicArray<DeliveryRequest> inorder = tree.inorderTraversal();
        assertSame(sr001, inorder.get(0));
        assertSame(replacement, inorder.get(1));
        assertSame(sr010, inorder.get(2));
        assertEquals("SR002", replacement.getRequestId());
        assertEquals(RequestStatus.PENDING, sr010.getStatus());
        assertEquals(3, tree.size());
        assertTrue(tree.isValidRedBlackTree());
    }

    @Test
    void largerAscendingDescendingAndMixedSequencesRemainSearchableAndOrdered() {
        CustomRedBlackTree<Integer, String> ascending = new CustomRedBlackTree<>();
        String[] expectedAscending = new String[16];
        for (int key = 1; key <= 16; key++) {
            ascending.insert(key, Integer.toString(key));
            assertTrue(ascending.isValidRedBlackTree());
            expectedAscending[key - 1] = Integer.toString(key);
        }
        assertSearchesPresent(ascending, 1, 16);
        assertArrayValues(ascending.inorderTraversal(), expectedAscending);
        assertTrue(ascending.height() <= redBlackHeightBound(ascending.size()));

        CustomRedBlackTree<Integer, String> descending = new CustomRedBlackTree<>();
        for (int key = 16; key >= 1; key--) {
            descending.insert(key, Integer.toString(key));
            assertTrue(descending.isValidRedBlackTree());
        }
        assertSearchesPresent(descending, 1, 16);
        assertArrayValues(descending.inorderTraversal(), expectedAscending);
        assertTrue(descending.height() <= redBlackHeightBound(descending.size()));

        CustomRedBlackTree<Integer, String> mixed = new CustomRedBlackTree<>();
        int[] mixedKeys = {50, 25, 75, 10, 30, 60, 80, 5, 15, 27, 65, 85};
        String[] expectedMixed = {"5", "10", "15", "25", "27", "30", "50", "60", "65", "75", "80", "85"};
        int expectedSize = 0;
        for (int key : mixedKeys) {
            mixed.insert(key, Integer.toString(key));
            expectedSize++;
            assertEquals(expectedSize, mixed.size());
            assertTrue(mixed.isValidRedBlackTree());
        }

        for (int key : mixedKeys) {
            assertEquals(Integer.toString(key), mixed.search(key));
        }
        assertArrayValues(mixed.inorderTraversal(), expectedMixed);
        assertTrue(mixed.height() <= redBlackHeightBound(mixed.size()));
    }

    private static CustomRedBlackTree<String, String> populatedLetterTree() {
        CustomRedBlackTree<String, String> tree = new CustomRedBlackTree<>();
        tree.insert("M", "middle");
        tree.insert("C", "charlie");
        tree.insert("T", "tango");
        tree.insert("A", "alpha");
        tree.insert("E", "echo");
        tree.insert("R", "romeo");
        tree.insert("Z", "zulu");
        return tree;
    }

    private static void assertValidSortedTree(int[] keys, String... expectedValues) {
        CustomRedBlackTree<Integer, String> tree = new CustomRedBlackTree<>();
        for (int key : keys) {
            tree.insert(key, Integer.toString(key));
            assertTrue(tree.isValidRedBlackTree());
        }

        assertArrayValues(tree.inorderTraversal(), expectedValues);
        assertTrue(tree.height() <= redBlackHeightBound(tree.size()));
    }

    private static DeliveryRequest request(String requestId, double priorityScore) {
        return new DeliveryRequest(
                requestId,
                "LOC001",
                "LOC002",
                "Food",
                1,
                1.0,
                BASE_TIME,
                BASE_TIME.plusHours(1),
                RequestStatus.PENDING,
                priorityScore);
    }

    private static void assertSearchesPresent(CustomRedBlackTree<Integer, String> tree, int first, int last) {
        for (int key = first; key <= last; key++) {
            assertEquals(Integer.toString(key), tree.search(key));
        }
    }

    private static void assertArrayValues(CustomDynamicArray<String> actual, String... expectedValues) {
        assertEquals(expectedValues.length, actual.size());
        for (int index = 0; index < expectedValues.length; index++) {
            assertEquals(expectedValues[index], actual.get(index));
        }
    }

    private static void assertContainsEachOnce(CustomDynamicArray<String> actual, String... expectedValues) {
        assertEquals(expectedValues.length, actual.size());
        for (String expectedValue : expectedValues) {
            int count = 0;
            for (int index = 0; index < actual.size(); index++) {
                if (expectedValue.equals(actual.get(index))) {
                    count++;
                }
            }
            assertEquals(1, count);
        }
    }

    private static int redBlackHeightBound(int size) {
        int value = size + 1;
        int floorLog = -1;
        while (value > 0) {
            floorLog++;
            value /= 2;
        }
        return 2 * floorLog;
    }

    private record RankKey(String label, int rank) implements Comparable<RankKey> {
        @Override
        public int compareTo(RankKey other) {
            return Integer.compare(rank, other.rank);
        }
    }
}
