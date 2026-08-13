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

class CustomBSTTest {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 2, 10, 8, 0);

    @Test
    void newTreeHasEmptyStateAndEmptyTraversals() {
        CustomBST<String, String> tree = new CustomBST<>();

        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        assertEquals(-1, tree.height());
        assertNull(tree.search("M"));
        assertFalse(tree.containsKey("M"));
        assertEquals(0, tree.inorderTraversal().size());
        assertEquals(0, tree.preorderTraversal().size());
        assertEquals(0, tree.postorderTraversal().size());
    }

    @Test
    void invalidNullArgumentsThrowIllegalArgumentException() {
        CustomBST<String, String> tree = new CustomBST<>();

        assertThrows(IllegalArgumentException.class, () -> tree.search(null));
        assertThrows(IllegalArgumentException.class, () -> tree.containsKey(null));
        assertThrows(IllegalArgumentException.class, () -> tree.remove(null));
        assertThrows(IllegalArgumentException.class, () -> tree.insert(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> tree.insert("key", null));
    }

    @Test
    void removingFromEmptyTreeThrowsIllegalArgumentException() {
        CustomBST<String, String> tree = new CustomBST<>();

        assertThrows(IllegalArgumentException.class, () -> tree.remove("missing"));
    }

    @Test
    void firstInsertionCreatesRootAndCanBeFound() {
        CustomBST<String, String> tree = new CustomBST<>();

        tree.insert("M", "middle");

        assertFalse(tree.isEmpty());
        assertEquals(1, tree.size());
        assertEquals(0, tree.height());
        assertEquals("middle", tree.search("M"));
        assertTrue(tree.containsKey("M"));
    }

    @Test
    void searchFindsLeftRightAndDeeperValues() {
        CustomBST<String, String> tree = populatedLetterTree();

        assertEquals("alpha", tree.search("A"));
        assertEquals("zulu", tree.search("Z"));
        assertEquals("charlie", tree.search("C"));
        assertNull(tree.search("0"));
        assertNull(tree.search("ZZ"));
        assertTrue(tree.containsKey("M"));
        assertFalse(tree.containsKey("Q"));
        assertEquals(7, tree.size());
    }

    @Test
    void integerKeysWorkCorrectly() {
        CustomBST<Integer, String> tree = new CustomBST<>();

        tree.insert(5, "five");
        tree.insert(2, "two");
        tree.insert(8, "eight");

        assertEquals("two", tree.search(2));
        assertEquals("five", tree.inorderTraversal().get(1));
    }

    @Test
    void customComparableKeysWorkCorrectly() {
        CustomBST<RankKey, String> tree = new CustomBST<>();

        tree.insert(new RankKey("normal", 3), "normal");
        tree.insert(new RankKey("urgent", 1), "urgent");
        tree.insert(new RankKey("soon", 2), "soon");

        assertEquals("urgent", tree.search(new RankKey("other", 1)));
        assertEquals("urgent", tree.inorderTraversal().get(0));
        assertEquals("soon", tree.inorderTraversal().get(1));
        assertEquals("normal", tree.inorderTraversal().get(2));
    }

    @Test
    void duplicateKeyReplacesValueWithoutChangingSizeOrShape() {
        CustomBST<String, String> tree = populatedLetterTree();
        int sizeBeforeUpdate = tree.size();
        int heightBeforeUpdate = tree.height();

        tree.insert("M", "replacement-root");
        tree.insert("A", "replacement-leaf");

        assertEquals(sizeBeforeUpdate, tree.size());
        assertEquals(heightBeforeUpdate, tree.height());
        assertEquals("replacement-root", tree.search("M"));
        assertEquals("replacement-leaf", tree.search("A"));
        assertEquals("charlie", tree.search("C"));
        assertEquals("zulu", tree.search("Z"));
        assertInorder(tree, "replacement-leaf", "charlie", "echo", "replacement-root", "romeo", "tango", "zulu");
    }

    @Test
    void compareToEqualDifferentKeyObjectsUpdateSameNode() {
        CustomBST<RankKey, String> tree = new CustomBST<>();

        tree.insert(new RankKey("first", 7), "first-value");
        tree.insert(new RankKey("second", 7), "replacement-value");

        assertEquals(1, tree.size());
        assertEquals("replacement-value", tree.search(new RankKey("third", 7)));
    }

    @Test
    void traversalsReturnExpectedOrdersAndCustomDynamicArrayResults() {
        CustomBST<String, String> tree = populatedLetterTree();
        CustomDynamicArray<String> inorder = tree.inorderTraversal();
        CustomDynamicArray<String> preorder = tree.preorderTraversal();
        CustomDynamicArray<String> postorder = tree.postorderTraversal();

        assertInstanceOf(CustomDynamicArray.class, inorder);
        assertEquals(tree.size(), inorder.size());
        assertArrayValues(inorder, "alpha", "charlie", "echo", "middle", "romeo", "tango", "zulu");
        assertArrayValues(preorder, "middle", "charlie", "alpha", "echo", "tango", "romeo", "zulu");
        assertArrayValues(postorder, "alpha", "echo", "charlie", "romeo", "zulu", "tango", "middle");
    }

    @Test
    void traversalResultsAreIndependentSnapshots() {
        CustomBST<String, String> tree = populatedLetterTree();
        CustomDynamicArray<String> earlierInorder = tree.inorderTraversal();

        earlierInorder.set(0, "changed-snapshot");
        tree.insert("B", "bravo");

        assertEquals("alpha", tree.search("A"));
        assertEquals("changed-snapshot", earlierInorder.get(0));
        assertEquals(7, earlierInorder.size());
        assertEquals(8, tree.inorderTraversal().size());
    }

    @Test
    void traversalsWorkAfterExistingKeyIsUpdated() {
        CustomBST<String, String> tree = populatedLetterTree();

        tree.insert("C", "updated-charlie");

        assertInorder(tree, "alpha", "updated-charlie", "echo", "middle", "romeo", "tango", "zulu");
    }

    @Test
    void deliveryRequestsAreIndexedAndTraversedByRequestId() {
        CustomBST<String, DeliveryRequest> tree = new CustomBST<>();
        DeliveryRequest sr010 = request("SR010", 10.0);
        DeliveryRequest sr002 = request("SR002", 20.0);
        DeliveryRequest sr001 = request("SR001", 30.0);
        DeliveryRequest replacement = request("SR002", 40.0);

        tree.insert(sr010.getRequestId(), sr010);
        tree.insert(sr002.getRequestId(), sr002);
        tree.insert(sr001.getRequestId(), sr001);

        assertSame(sr002, tree.search("SR002"));
        tree.insert("SR002", replacement);

        CustomDynamicArray<DeliveryRequest> inorder = tree.inorderTraversal();
        assertSame(sr001, inorder.get(0));
        assertSame(replacement, inorder.get(1));
        assertSame(sr010, inorder.get(2));
        assertEquals(3, tree.size());
    }

    @Test
    void removingLeafReturnsValueAndPreservesOtherNodes() {
        CustomBST<String, String> tree = populatedLetterTree();

        assertEquals("alpha", tree.remove("A"));

        assertEquals(6, tree.size());
        assertNull(tree.search("A"));
        assertEquals("charlie", tree.search("C"));
        assertEquals("zulu", tree.search("Z"));
        assertInorder(tree, "charlie", "echo", "middle", "romeo", "tango", "zulu");
    }

    @Test
    void removingNodeWithOnlyLeftChildReconnectsChild() {
        CustomBST<String, String> tree = new CustomBST<>();
        tree.insert("M", "middle");
        tree.insert("C", "charlie");
        tree.insert("B", "bravo");

        assertEquals("charlie", tree.remove("C"));

        assertEquals(2, tree.size());
        assertEquals("bravo", tree.search("B"));
        assertNull(tree.search("C"));
        assertInorder(tree, "bravo", "middle");
    }

    @Test
    void removingNodeWithOnlyRightChildReconnectsChild() {
        CustomBST<String, String> tree = new CustomBST<>();
        tree.insert("M", "middle");
        tree.insert("C", "charlie");
        tree.insert("E", "echo");

        assertEquals("charlie", tree.remove("C"));

        assertEquals(2, tree.size());
        assertEquals("echo", tree.search("E"));
        assertNull(tree.search("C"));
        assertInorder(tree, "echo", "middle");
    }

    @Test
    void removingNonRootNodeWithTwoChildrenUsesSuccessorAndPreservesOrdering() {
        CustomBST<String, String> tree = populatedLetterTree();

        assertEquals("charlie", tree.remove("C"));

        assertEquals(6, tree.size());
        assertNull(tree.search("C"));
        assertEquals("echo", tree.search("E"));
        assertEquals("alpha", tree.search("A"));
        assertInorder(tree, "alpha", "echo", "middle", "romeo", "tango", "zulu");
    }

    @Test
    void removingRootWithTwoChildrenReturnsOriginalValueAndRemovesSuccessorNode() {
        CustomBST<String, String> tree = populatedLetterTree();

        assertEquals("middle", tree.remove("M"));

        assertEquals(6, tree.size());
        assertNull(tree.search("M"));
        assertEquals("romeo", tree.search("R"));
        assertEquals("tango", tree.search("T"));
        assertInorder(tree, "alpha", "charlie", "echo", "romeo", "tango", "zulu");
    }

    @Test
    void removingOnlyNodeLeavesTreeEmptyAndReusable() {
        CustomBST<String, String> tree = new CustomBST<>();
        tree.insert("M", "middle");

        assertEquals("middle", tree.remove("M"));

        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        assertEquals(-1, tree.height());
        assertNull(tree.search("M"));
        tree.insert("A", "alpha");
        assertEquals("alpha", tree.search("A"));
    }

    @Test
    void removingRootWithOnlyLeftChildPromotesLeftChild() {
        CustomBST<String, String> tree = new CustomBST<>();
        tree.insert("M", "middle");
        tree.insert("C", "charlie");

        assertEquals("middle", tree.remove("M"));

        assertEquals("charlie", tree.search("C"));
        assertInorder(tree, "charlie");
    }

    @Test
    void removingRootWithOnlyRightChildPromotesRightChild() {
        CustomBST<String, String> tree = new CustomBST<>();
        tree.insert("M", "middle");
        tree.insert("T", "tango");

        assertEquals("middle", tree.remove("M"));

        assertEquals("tango", tree.search("T"));
        assertInorder(tree, "tango");
    }

    @Test
    void failedRemovalLeavesSizeAndTraversalUnchanged() {
        CustomBST<String, String> tree = populatedLetterTree();
        CustomDynamicArray<String> before = tree.inorderTraversal();
        int sizeBefore = tree.size();

        assertThrows(IllegalArgumentException.class, () -> tree.remove("Q"));

        assertEquals(sizeBefore, tree.size());
        assertArrayValues(tree.inorderTraversal(), before);
    }

    @Test
    void heightUsesEdgeBasedConventionForBalancedAndSkewedTrees() {
        CustomBST<Integer, String> balanced = new CustomBST<>();
        insertKeysAsStrings(balanced, 2, 1, 3);
        assertEquals(1, balanced.height());

        CustomBST<Integer, String> largerBalanced = new CustomBST<>();
        insertKeysAsStrings(largerBalanced, 4, 2, 6, 1, 3, 5, 7);
        assertEquals(2, largerBalanced.height());

        CustomBST<Integer, String> rightSkewed = new CustomBST<>();
        insertKeysAsStrings(rightSkewed, 1, 2, 3, 4);
        assertEquals(3, rightSkewed.height());

        CustomBST<Integer, String> leftSkewed = new CustomBST<>();
        insertKeysAsStrings(leftSkewed, 4, 3, 2, 1);
        assertEquals(3, leftSkewed.height());
    }

    @Test
    void mixedHeightRemovalAndDuplicateUpdateBehaveAsExpected() {
        CustomBST<Integer, String> tree = new CustomBST<>();
        insertKeysAsStrings(tree, 10, 5, 20, 15, 25, 13);
        assertEquals(3, tree.height());

        tree.insert(13, "updated-13");
        assertEquals(3, tree.height());
        assertEquals(6, tree.size());

        assertEquals("updated-13", tree.remove(13));
        assertEquals(2, tree.height());
    }

    @Test
    void clearEmptiesTreeAndAllowsReuse() {
        CustomBST<String, String> tree = populatedLetterTree();

        tree.clear();

        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        assertEquals(-1, tree.height());
        assertNull(tree.search("M"));
        assertEquals(0, tree.inorderTraversal().size());
        assertEquals(0, tree.preorderTraversal().size());
        assertEquals(0, tree.postorderTraversal().size());
        tree.clear();
        tree.insert("N", "new");
        assertEquals("new", tree.search("N"));
    }

    @Test
    void removingDeliveryRequestIndexEntryDoesNotMutateRequestObject() {
        CustomBST<String, DeliveryRequest> tree = new CustomBST<>();
        DeliveryRequest request = request("SR001", 15.0);

        tree.insert(request.getRequestId(), request);

        assertSame(request, tree.remove("SR001"));
        assertEquals("SR001", request.getRequestId());
        assertEquals(RequestStatus.PENDING, request.getStatus());
        assertEquals(15.0, request.getPriorityScore());
    }

    @Test
    void largerDeterministicSequenceSupportsSearchSortedTraversalAndRepeatedRemoval() {
        CustomBST<Integer, String> tree = new CustomBST<>();
        int[] keys = {50, 25, 75, 10, 30, 60, 80, 5, 15, 27, 65, 85};
        String[] expectedSortedValues = {"5", "10", "15", "25", "27", "30", "50", "60", "65", "75", "80", "85"};
        int[] removalOrder = {25, 80, 50, 5, 85, 10, 30, 60, 75, 15, 27, 65};

        insertKeysAsStrings(tree, keys);

        for (int key : keys) {
            assertEquals(Integer.toString(key), tree.search(key));
        }
        assertArrayValues(tree.inorderTraversal(), expectedSortedValues);

        int expectedSize = keys.length;
        for (int key : removalOrder) {
            String removed = tree.remove(key);
            expectedSize--;
            assertEquals(Integer.toString(key), removed);
            assertEquals(expectedSize, tree.size());
            assertInAscendingOrder(tree.inorderTraversal());
        }

        assertTrue(tree.isEmpty());
        assertEquals(-1, tree.height());
    }

    private static CustomBST<String, String> populatedLetterTree() {
        CustomBST<String, String> tree = new CustomBST<>();
        tree.insert("M", "middle");
        tree.insert("C", "charlie");
        tree.insert("T", "tango");
        tree.insert("A", "alpha");
        tree.insert("E", "echo");
        tree.insert("R", "romeo");
        tree.insert("Z", "zulu");
        return tree;
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

    private static void insertKeysAsStrings(CustomBST<Integer, String> tree, int... keys) {
        for (int key : keys) {
            tree.insert(key, Integer.toString(key));
        }
    }

    private static void assertInorder(CustomBST<String, String> tree, String... expectedValues) {
        assertArrayValues(tree.inorderTraversal(), expectedValues);
    }

    private static void assertArrayValues(CustomDynamicArray<String> actual, String... expectedValues) {
        assertEquals(expectedValues.length, actual.size());
        for (int index = 0; index < expectedValues.length; index++) {
            assertEquals(expectedValues[index], actual.get(index));
        }
    }

    private static void assertArrayValues(CustomDynamicArray<String> actual, CustomDynamicArray<String> expected) {
        assertEquals(expected.size(), actual.size());
        for (int index = 0; index < expected.size(); index++) {
            assertEquals(expected.get(index), actual.get(index));
        }
    }

    private static void assertInAscendingOrder(CustomDynamicArray<String> values) {
        for (int index = 1; index < values.size(); index++) {
            int previous = Integer.parseInt(values.get(index - 1));
            int current = Integer.parseInt(values.get(index));
            assertTrue(previous < current);
        }
    }

    private record RankKey(String label, int rank) implements Comparable<RankKey> {
        @Override
        public int compareTo(RankKey other) {
            return Integer.compare(rank, other.rank);
        }
    }
}
