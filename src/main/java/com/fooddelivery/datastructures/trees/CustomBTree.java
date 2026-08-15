package com.fooddelivery.datastructures.trees;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;

/**
 * Fixed-degree B-tree implementation for indexed lookup workloads.
 *
 * @param <K> comparable key type
 * @param <V> stored value type
 */
public class CustomBTree<K extends Comparable<? super K>, V> {
    private static final int MINIMUM_DEGREE = 3;
    private static final int MAXIMUM_KEYS = (2 * MINIMUM_DEGREE) - 1;
    private static final int MAXIMUM_CHILDREN = 2 * MINIMUM_DEGREE;
    private static final int MINIMUM_NON_ROOT_KEYS = MINIMUM_DEGREE - 1;
    private static final int MEDIAN_INDEX = MINIMUM_DEGREE - 1;

    private Node<K, V> root;
    private int size;

    /**
     * Inserts a key-value pair in O(h) time because the degree is fixed at 3.
     * Existing logical keys are updated without changing the tree shape.
     *
     * @param key non-null key
     * @param value non-null value
     */
    public void insert(K key, V value) {
        validateKey(key);
        validateValue(value);

        SearchResult<K, V> existing = findNodeWithKey(root, key);
        if (existing != null) {
            existing.node.values[existing.index] = value;
            return;
        }

        if (root == null) {
            root = new Node<>(true);
            root.keys[0] = key;
            root.values[0] = value;
            root.keyCount = 1;
            size = 1;
            return;
        }

        if (root.keyCount == MAXIMUM_KEYS) {
            Node<K, V> newRoot = new Node<>(false);
            newRoot.children[0] = root;
            splitChild(newRoot, 0);
            root = newRoot;
        }

        insertNonFull(root, key, value);
        size++;
    }

    /**
     * Searches for a key in O(h) time because the degree is fixed at 3.
     *
     * @param key non-null key
     * @return value for the key, or null when absent
     */
    public V search(K key) {
        validateKey(key);
        SearchResult<K, V> result = findNodeWithKey(root, key);
        return result == null ? null : result.node.values[result.index];
    }

    /**
     * Checks key presence in O(h) time because the degree is fixed at 3.
     *
     * @param key non-null key
     * @return true when the key exists
     */
    public boolean containsKey(K key) {
        validateKey(key);
        return findNodeWithKey(root, key) != null;
    }

    /**
     * Traverses all values in ascending key order in O(n) time.
     *
     * @return independent dynamic array of values
     */
    public CustomDynamicArray<V> traverse() {
        CustomDynamicArray<V> values = new CustomDynamicArray<>();
        traverseInOrder(root, values);
        return values;
    }

    /**
     * Computes edge-based tree height in O(h) time.
     *
     * @return -1 for empty, 0 for a leaf root
     */
    public int height() {
        if (root == null) {
            return -1;
        }

        int height = 0;
        Node<K, V> current = root;
        while (!current.leaf) {
            height++;
            current = current.children[0];
        }
        return height;
    }

    /**
     * Returns the number of stored keys in O(1) time.
     *
     * @return stored key count
     */
    public int size() {
        return size;
    }

    /**
     * Checks emptiness in O(1) time.
     *
     * @return true when the tree has no keys
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Clears the tree in O(1) time.
     */
    public void clear() {
        root = null;
        size = 0;
    }

    /**
     * Validates the B-tree invariants in O(n) time without mutating the tree.
     *
     * @return true when all invariants hold
     */
    public boolean isValidBTree() {
        if (root == null) {
            return size == 0;
        }

        ValidationResult result = validateNode(root, null, null, 0, true);
        return result.valid && result.keyCount == size;
    }

    private void insertNonFull(Node<K, V> node, K key, V value) {
        int index = node.keyCount - 1;

        if (node.leaf) {
            while (index >= 0 && key.compareTo(node.keys[index]) < 0) {
                node.keys[index + 1] = node.keys[index];
                node.values[index + 1] = node.values[index];
                index--;
            }

            node.keys[index + 1] = key;
            node.values[index + 1] = value;
            node.keyCount++;
            return;
        }

        while (index >= 0 && key.compareTo(node.keys[index]) < 0) {
            index--;
        }
        index++;

        if (node.children[index].keyCount == MAXIMUM_KEYS) {
            splitChild(node, index);
            if (key.compareTo(node.keys[index]) > 0) {
                index++;
            }
        }

        insertNonFull(node.children[index], key, value);
    }

    private void splitChild(Node<K, V> parent, int childIndex) {
        Node<K, V> child = parent.children[childIndex];
        Node<K, V> sibling = new Node<>(child.leaf);
        K promotedKey = child.keys[MEDIAN_INDEX];
        V promotedValue = child.values[MEDIAN_INDEX];

        sibling.keyCount = MINIMUM_NON_ROOT_KEYS;
        for (int i = 0; i < MINIMUM_NON_ROOT_KEYS; i++) {
            int sourceIndex = i + MINIMUM_DEGREE;
            sibling.keys[i] = child.keys[sourceIndex];
            sibling.values[i] = child.values[sourceIndex];
            child.keys[sourceIndex] = null;
            child.values[sourceIndex] = null;
        }

        if (!child.leaf) {
            for (int i = 0; i < MINIMUM_DEGREE; i++) {
                int sourceIndex = i + MINIMUM_DEGREE;
                sibling.children[i] = child.children[sourceIndex];
                child.children[sourceIndex] = null;
            }
        }

        child.keys[MEDIAN_INDEX] = null;
        child.values[MEDIAN_INDEX] = null;
        child.keyCount = MINIMUM_NON_ROOT_KEYS;

        for (int i = parent.keyCount; i >= childIndex + 1; i--) {
            parent.children[i + 1] = parent.children[i];
        }
        parent.children[childIndex + 1] = sibling;

        for (int i = parent.keyCount - 1; i >= childIndex; i--) {
            parent.keys[i + 1] = parent.keys[i];
            parent.values[i + 1] = parent.values[i];
        }

        parent.keys[childIndex] = promotedKey;
        parent.values[childIndex] = promotedValue;
        parent.keyCount++;
    }

    private SearchResult<K, V> findNodeWithKey(Node<K, V> node, K key) {
        Node<K, V> current = node;
        while (current != null) {
            int index = firstGreaterThanOrEqual(current, key);
            if (index < current.keyCount && key.compareTo(current.keys[index]) == 0) {
                return new SearchResult<>(current, index);
            }
            if (current.leaf) {
                return null;
            }
            current = current.children[index];
        }
        return null;
    }

    private int firstGreaterThanOrEqual(Node<K, V> node, K key) {
        int index = 0;
        while (index < node.keyCount && key.compareTo(node.keys[index]) > 0) {
            index++;
        }
        return index;
    }

    private void traverseInOrder(Node<K, V> node, CustomDynamicArray<V> values) {
        if (node == null) {
            return;
        }

        for (int i = 0; i < node.keyCount; i++) {
            if (!node.leaf) {
                traverseInOrder(node.children[i], values);
            }
            values.add(node.values[i]);
        }

        if (!node.leaf) {
            traverseInOrder(node.children[node.keyCount], values);
        }
    }

    private ValidationResult validateNode(Node<K, V> node, K lowerBound, K upperBound, int depth, boolean rootNode) {
        if (node == null || depth > size + 1) {
            return ValidationResult.invalid();
        }

        if (rootNode) {
            if (node.keyCount < 1 || node.keyCount > MAXIMUM_KEYS) {
                return ValidationResult.invalid();
            }
        } else if (node.keyCount < MINIMUM_NON_ROOT_KEYS || node.keyCount > MAXIMUM_KEYS) {
            return ValidationResult.invalid();
        }

        for (int i = 0; i < node.keyCount; i++) {
            if (node.keys[i] == null || node.values[i] == null) {
                return ValidationResult.invalid();
            }
            if (i > 0 && node.keys[i - 1].compareTo(node.keys[i]) >= 0) {
                return ValidationResult.invalid();
            }
            if (lowerBound != null && node.keys[i].compareTo(lowerBound) <= 0) {
                return ValidationResult.invalid();
            }
            if (upperBound != null && node.keys[i].compareTo(upperBound) >= 0) {
                return ValidationResult.invalid();
            }
        }

        for (int i = node.keyCount; i < MAXIMUM_KEYS; i++) {
            if (node.keys[i] != null || node.values[i] != null) {
                return ValidationResult.invalid();
            }
        }

        if (node.leaf) {
            for (int i = 0; i < MAXIMUM_CHILDREN; i++) {
                if (node.children[i] != null) {
                    return ValidationResult.invalid();
                }
            }
            return ValidationResult.valid(node.keyCount, depth);
        }

        int totalKeys = node.keyCount;
        int expectedLeafDepth = -1;
        for (int i = 0; i <= node.keyCount; i++) {
            if (node.children[i] == null) {
                return ValidationResult.invalid();
            }

            K childLowerBound = i == 0 ? lowerBound : node.keys[i - 1];
            K childUpperBound = i == node.keyCount ? upperBound : node.keys[i];
            ValidationResult childResult = validateNode(node.children[i], childLowerBound, childUpperBound, depth + 1, false);
            if (!childResult.valid) {
                return ValidationResult.invalid();
            }

            if (expectedLeafDepth == -1) {
                expectedLeafDepth = childResult.leafDepth;
            } else if (expectedLeafDepth != childResult.leafDepth) {
                return ValidationResult.invalid();
            }
            totalKeys += childResult.keyCount;
        }

        for (int i = node.keyCount + 1; i < MAXIMUM_CHILDREN; i++) {
            if (node.children[i] != null) {
                return ValidationResult.invalid();
            }
        }

        return ValidationResult.valid(totalKeys, expectedLeafDepth);
    }

    private void validateKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
    }

    private void validateValue(V value) {
        if (value == null) {
            throw new IllegalArgumentException("Value must not be null");
        }
    }

    private static final class Node<K extends Comparable<? super K>, V> {
        private final K[] keys;
        private final V[] values;
        private final Node<K, V>[] children;
        private int keyCount;
        private final boolean leaf;

        @SuppressWarnings("unchecked")
        private Node(boolean leaf) {
            this.keys = (K[]) new Comparable[MAXIMUM_KEYS];
            this.values = (V[]) new Object[MAXIMUM_KEYS];
            this.children = (Node<K, V>[]) new Node[MAXIMUM_CHILDREN];
            this.leaf = leaf;
        }
    }

    private static final class SearchResult<K extends Comparable<? super K>, V> {
        private final Node<K, V> node;
        private final int index;

        private SearchResult(Node<K, V> node, int index) {
            this.node = node;
            this.index = index;
        }
    }

    private static final class ValidationResult {
        private final boolean valid;
        private final int keyCount;
        private final int leafDepth;

        private ValidationResult(boolean valid, int keyCount, int leafDepth) {
            this.valid = valid;
            this.keyCount = keyCount;
            this.leafDepth = leafDepth;
        }

        private static ValidationResult valid(int keyCount, int leafDepth) {
            return new ValidationResult(true, keyCount, leafDepth);
        }

        private static ValidationResult invalid() {
            return new ValidationResult(false, 0, -1);
        }
    }
}
