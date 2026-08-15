package com.fooddelivery.datastructures.trees;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.RequestStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomBTreeTest {
    @Test
    void emptyTreeHasExpectedState() {
        CustomBTree<String, String> tree = new CustomBTree<>();

        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        assertEquals(-1, tree.height());
        assertTrue(tree.isValidBTree());
        assertEquals(0, tree.traverse().size());
        assertNull(tree.search("missing"));
        assertFalse(tree.containsKey("missing"));
    }

    @Test
    void rejectsNullKeysAndValues() {
        CustomBTree<String, String> tree = new CustomBTree<>();

        assertThrows(IllegalArgumentException.class, () -> tree.insert(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> tree.insert("key", null));
        assertThrows(IllegalArgumentException.class, () -> tree.search(null));
        assertThrows(IllegalArgumentException.class, () -> tree.containsKey(null));
        assertTrue(tree.isEmpty());
        assertTrue(tree.isValidBTree());
    }

    @Test
    void insertsSingleKeyIntoLeafRoot() {
        CustomBTree<String, String> tree = new CustomBTree<>();

        tree.insert("REQ-001", "first");

        assertFalse(tree.isEmpty());
        assertEquals(1, tree.size());
        assertEquals(0, tree.height());
        assertEquals("first", tree.search("REQ-001"));
        assertTrue(tree.containsKey("REQ-001"));
        assertTrue(tree.isValidBTree());
        assertTraversal(tree, "first");
    }

    @Test
    void keepsLeafRootSortedForAscendingDescendingAndMixedInsertions() {
        CustomBTree<Integer, String> ascending = new CustomBTree<>();
        CustomBTree<Integer, String> descending = new CustomBTree<>();
        CustomBTree<Integer, String> mixed = new CustomBTree<>();

        for (int i = 1; i <= 5; i++) {
            ascending.insert(i, "v" + i);
            descending.insert(6 - i, "v" + (6 - i));
        }
        int[] mixedKeys = {3, 1, 5, 2, 4};
        for (int key : mixedKeys) {
            mixed.insert(key, "v" + key);
        }

        assertTreeRange(ascending, 1, 5, 0);
        assertTreeRange(descending, 1, 5, 0);
        assertTreeRange(mixed, 1, 5, 0);
    }

    @Test
    void splitsFullRootBeforeDescending() {
        CustomBTree<Integer, String> tree = new CustomBTree<>();

        for (int i = 1; i <= 6; i++) {
            tree.insert(i, "v" + i);
        }

        assertTreeRange(tree, 1, 6, 1);
    }

    @Test
    void supportsRootSplitFromDescendingInput() {
        CustomBTree<Integer, String> tree = new CustomBTree<>();

        for (int i = 6; i >= 1; i--) {
            tree.insert(i, "v" + i);
        }

        assertTreeRange(tree, 1, 6, 1);
    }

    @Test
    void splitsFullChildrenWhilePreservingAllValues() {
        CustomBTree<Integer, String> tree = new CustomBTree<>();

        for (int i = 1; i <= 20; i++) {
            tree.insert(i, "v" + i);
            assertTrue(tree.isValidBTree());
            assertEquals(i, tree.size());
        }

        assertTreeRange(tree, 1, 20, 2);
    }

    @Test
    void growsBeyondRootHeightWhenSecondLevelFills() {
        CustomBTree<Integer, String> tree = new CustomBTree<>();

        for (int i = 1; i <= 40; i++) {
            tree.insert(i, "v" + i);
            assertTrue(tree.isValidBTree());
        }

        assertTreeRange(tree, 1, 40, 2);
    }

    @Test
    void updatesDuplicateKeyWithoutChangingSizeOrHeight() {
        CustomBTree<Integer, String> tree = new CustomBTree<>();
        for (int i = 1; i <= 12; i++) {
            tree.insert(i, "v" + i);
        }
        int sizeBefore = tree.size();
        int heightBefore = tree.height();

        tree.insert(3, "updated-promoted");
        tree.insert(12, "updated-leaf");

        assertEquals(sizeBefore, tree.size());
        assertEquals(heightBefore, tree.height());
        assertEquals("updated-promoted", tree.search(3));
        assertEquals("updated-leaf", tree.search(12));
        assertTrue(tree.isValidBTree());
    }

    @Test
    void treatsCompareToEqualKeysAsDuplicates() {
        CustomBTree<ComparableRequestKey, String> tree = new CustomBTree<>();
        ComparableRequestKey first = new ComparableRequestKey("REQ-A", 10);
        ComparableRequestKey equivalent = new ComparableRequestKey("REQ-B", 10);

        tree.insert(first, "original");
        tree.insert(equivalent, "replacement");

        assertEquals(1, tree.size());
        assertEquals("replacement", tree.search(first));
        assertEquals("replacement", tree.search(equivalent));
        assertTrue(tree.isValidBTree());
        assertTraversal(tree, "replacement");
    }

    @Test
    void traversalReturnsIndependentDynamicArray() {
        CustomBTree<Integer, String> tree = new CustomBTree<>();
        for (int i = 1; i <= 8; i++) {
            tree.insert(i, "v" + i);
        }

        CustomDynamicArray<String> firstTraversal = tree.traverse();
        firstTraversal.set(0, "changed");
        CustomDynamicArray<String> secondTraversal = tree.traverse();

        assertEquals("changed", firstTraversal.get(0));
        assertEquals("v1", secondTraversal.get(0));
        assertNotSame(firstTraversal, secondTraversal);
        assertTreeRange(tree, 1, 8, 1);
    }

    @Test
    void clearResetsTreeAndAllowsReuse() {
        CustomBTree<Integer, String> tree = new CustomBTree<>();
        for (int i = 1; i <= 10; i++) {
            tree.insert(i, "v" + i);
        }

        tree.clear();

        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        assertEquals(-1, tree.height());
        assertTrue(tree.isValidBTree());
        assertNull(tree.search(1));

        tree.insert(42, "answer");
        assertEquals(1, tree.size());
        assertEquals(0, tree.height());
        assertEquals("answer", tree.search(42));
        assertTrue(tree.isValidBTree());
    }

    @Test
    void handlesDeliveryRequestLookupByRequestId() {
        CustomBTree<String, DeliveryRequest> tree = new CustomBTree<>();
        DeliveryRequest[] requests = new DeliveryRequest[12];

        for (int i = 0; i < requests.length; i++) {
            String requestId = requestId(i + 1);
            requests[i] = request(requestId, i + 1);
            tree.insert(requestId, requests[i]);
        }

        assertEquals(12, tree.size());
        assertEquals(1, tree.height());
        assertTrue(tree.isValidBTree());
        for (int i = 0; i < requests.length; i++) {
            assertSame(requests[i], tree.search(requestId(i + 1)));
        }
        assertDeliveryTraversal(tree, requests);
    }

    @Test
    void updatesDeliveryRequestWithoutDuplicatingRequestId() {
        CustomBTree<String, DeliveryRequest> tree = new CustomBTree<>();
        DeliveryRequest original = request("REQ-006", 6);
        DeliveryRequest replacement = request("REQ-006", 60);

        for (int i = 1; i <= 10; i++) {
            tree.insert(requestId(i), request(requestId(i), i));
        }
        int sizeBefore = tree.size();
        int heightBefore = tree.height();

        tree.insert(original.getRequestId(), original);
        tree.insert(replacement.getRequestId(), replacement);

        assertEquals(sizeBefore, tree.size());
        assertEquals(heightBefore, tree.height());
        assertSame(replacement, tree.search("REQ-006"));
        assertTrue(tree.isValidBTree());
    }

    @Test
    void missingSearchesRemainNullAfterMultipleSplits() {
        CustomBTree<Integer, String> tree = new CustomBTree<>();

        for (int i = 2; i <= 80; i += 2) {
            tree.insert(i, "v" + i);
        }

        assertNull(tree.search(1));
        assertNull(tree.search(41));
        assertNull(tree.search(99));
        assertFalse(tree.containsKey(41));
        assertTrue(tree.containsKey(40));
        assertTrue(tree.isValidBTree());
    }

    @Test
    void mixedInsertionSequenceMaintainsAscendingTraversal() {
        CustomBTree<Integer, String> tree = new CustomBTree<>();
        int[] keys = {
                25, 10, 40, 5, 15, 30, 45, 1, 8, 12,
                18, 28, 33, 42, 50, 3, 6, 11, 14, 16,
                20, 27, 29, 31, 35
        };

        for (int i = 0; i < keys.length; i++) {
            tree.insert(keys[i], "v" + keys[i]);
            assertTrue(tree.isValidBTree());
            assertEquals(i + 1, tree.size());
        }

        assertTraversal(tree,
                "v1", "v3", "v5", "v6", "v8",
                "v10", "v11", "v12", "v14", "v15",
                "v16", "v18", "v20", "v25", "v27",
                "v28", "v29", "v30", "v31", "v33",
                "v35", "v40", "v42", "v45", "v50");
        for (int key : keys) {
            assertEquals("v" + key, tree.search(key));
        }
        assertTrue(tree.height() >= 1);
        assertTrue(tree.isValidBTree());
    }

    private void assertTreeRange(CustomBTree<Integer, String> tree, int first, int last, int expectedHeight) {
        assertEquals(last - first + 1, tree.size());
        assertEquals(expectedHeight, tree.height());
        assertTrue(tree.isValidBTree());
        for (int i = first; i <= last; i++) {
            assertEquals("v" + i, tree.search(i));
            assertTrue(tree.containsKey(i));
        }
        assertTraversal(tree, expectedValues(first, last));
    }

    private String[] expectedValues(int first, int last) {
        String[] values = new String[last - first + 1];
        for (int i = first; i <= last; i++) {
            values[i - first] = "v" + i;
        }
        return values;
    }

    private void assertTraversal(CustomBTree<?, String> tree, String... expectedValues) {
        CustomDynamicArray<String> values = tree.traverse();
        assertEquals(expectedValues.length, values.size());
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(expectedValues[i], values.get(i));
        }
    }

    private void assertDeliveryTraversal(CustomBTree<String, DeliveryRequest> tree, DeliveryRequest[] expectedRequests) {
        CustomDynamicArray<DeliveryRequest> values = tree.traverse();
        assertEquals(expectedRequests.length, values.size());
        for (int i = 0; i < expectedRequests.length; i++) {
            assertSame(expectedRequests[i], values.get(i));
        }
    }

    private DeliveryRequest request(String requestId, int index) {
        LocalDateTime submitted = LocalDateTime.of(2026, 1, 1, 9, 0).plusMinutes(index);
        return new DeliveryRequest(
                requestId,
                "LOC-" + index,
                "LOC-" + (index + 1),
                "GROCERY",
                (index % 5) + 1,
                2.5 + index,
                submitted,
                submitted.plusHours(2),
                RequestStatus.PENDING,
                10.0 + index
        );
    }

    private String requestId(int number) {
        if (number < 10) {
            return "REQ-00" + number;
        }
        return "REQ-0" + number;
    }

    private static final class ComparableRequestKey implements Comparable<ComparableRequestKey> {
        private final String label;
        private final int rank;

        private ComparableRequestKey(String label, int rank) {
            this.label = label;
            this.rank = rank;
        }

        @Override
        public int compareTo(ComparableRequestKey other) {
            return Integer.compare(rank, other.rank);
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
