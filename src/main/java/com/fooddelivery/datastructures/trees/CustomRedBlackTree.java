package com.fooddelivery.datastructures.trees;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;

/**
 * Generic insertion-balanced red-black tree for key-value indexing.
 *
 * <p>Keys are ordered exclusively with {@link Comparable#compareTo(Object)}.
 * Duplicate keys replace the stored value without changing the tree structure
 * or size. Conceptual null leaves are treated as black leaves; no sentinel node
 * is exposed or stored.</p>
 *
 * <p>This phase supports insertion, search, traversal, height, clear and a
 * linear-time correctness diagnostic. Deletion is intentionally not implemented.
 * While red-black invariants hold, insert, search and containsKey run in
 * O(log n). Rotations are O(1). Traversals, height calculation and invariant
 * validation are O(n), and comparison cost depends on the key type.</p>
 *
 * @param <K> comparable key type
 * @param <V> stored value type
 */
public class CustomRedBlackTree<K extends Comparable<? super K>, V> {
    private Node<K, V> root;
    private int size;

    /**
     * Inserts a key-value entry, restoring red-black invariants after new nodes.
     *
     * @param key comparable key
     * @param value non-null value
     * @throws IllegalArgumentException if key or value is null
     */
    public void insert(K key, V value) {
        validateKey(key);
        validateValue(value);

        if (root == null) {
            root = new Node<>(key, value, Color.BLACK);
            size = 1;
            return;
        }

        Node<K, V> parent = null;
        Node<K, V> current = root;
        int comparison = 0;
        while (current != null) {
            parent = current;
            comparison = key.compareTo(current.key);
            if (comparison == 0) {
                current.value = value;
                return;
            }

            current = comparison < 0 ? current.left : current.right;
        }

        Node<K, V> inserted = new Node<>(key, value, Color.RED);
        inserted.parent = parent;
        if (comparison < 0) {
            parent.left = inserted;
        } else {
            parent.right = inserted;
        }

        size++;
        fixAfterInsertion(inserted);
    }

    /**
     * Searches for a key in O(log n) while the tree remains balanced.
     *
     * @param key key to find
     * @return stored value, or null when the valid key is absent
     * @throws IllegalArgumentException if key is null
     */
    public V search(K key) {
        Node<K, V> node = findNode(key);
        return node == null ? null : node.value;
    }

    /**
     * Returns whether a key exists in O(log n) while the tree remains balanced.
     *
     * @param key key to find
     * @return true when present
     * @throws IllegalArgumentException if key is null
     */
    public boolean containsKey(K key) {
        return findNode(key) != null;
    }

    /**
     * Returns values in ascending key order in O(n).
     */
    public CustomDynamicArray<V> inorderTraversal() {
        CustomDynamicArray<V> values = new CustomDynamicArray<>();
        addInorder(root, values);
        return values;
    }

    /**
     * Returns values in root-left-right order in O(n).
     */
    public CustomDynamicArray<V> preorderTraversal() {
        CustomDynamicArray<V> values = new CustomDynamicArray<>();
        addPreorder(root, values);
        return values;
    }

    /**
     * Returns values in left-right-root order in O(n).
     */
    public CustomDynamicArray<V> postorderTraversal() {
        CustomDynamicArray<V> values = new CustomDynamicArray<>();
        addPostorder(root, values);
        return values;
    }

    /**
     * Returns edge-based height in O(n): empty is -1 and one node is 0.
     */
    public int height() {
        return height(root);
    }

    /**
     * Returns the number of key-value entries in O(1).
     */
    public int size() {
        return size;
    }

    /**
     * Returns true exactly when the tree has no entries in O(1).
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all entries in O(1) by dropping the root reference.
     */
    public void clear() {
        root = null;
        size = 0;
    }

    /**
     * Validates red-black, BST and parent-reference invariants in O(n).
     *
     * @return true when the current tree satisfies the expected invariants
     */
    public boolean isValidRedBlackTree() {
        if (root == null) {
            return size == 0;
        }

        if (root.color != Color.BLACK || root.parent != null) {
            return false;
        }

        ValidationResult result = validateSubtree(root, null, null, 0);
        return result.valid && result.nodeCount == size;
    }

    private void fixAfterInsertion(Node<K, V> node) {
        Node<K, V> current = node;
        while (current != root && colorOf(parentOf(current)) == Color.RED) {
            Node<K, V> parent = parentOf(current);
            Node<K, V> grandparent = parentOf(parent);

            if (parent == grandparent.left) {
                Node<K, V> uncle = grandparent.right;
                if (colorOf(uncle) == Color.RED) {
                    parent.color = Color.BLACK;
                    uncle.color = Color.BLACK;
                    grandparent.color = Color.RED;
                    current = grandparent;
                } else {
                    if (current == parent.right) {
                        current = parent;
                        rotateLeft(current);
                        parent = parentOf(current);
                        grandparent = parentOf(parent);
                    }

                    parent.color = Color.BLACK;
                    grandparent.color = Color.RED;
                    rotateRight(grandparent);
                }
            } else {
                Node<K, V> uncle = grandparent.left;
                if (colorOf(uncle) == Color.RED) {
                    parent.color = Color.BLACK;
                    uncle.color = Color.BLACK;
                    grandparent.color = Color.RED;
                    current = grandparent;
                } else {
                    if (current == parent.left) {
                        current = parent;
                        rotateRight(current);
                        parent = parentOf(current);
                        grandparent = parentOf(parent);
                    }

                    parent.color = Color.BLACK;
                    grandparent.color = Color.RED;
                    rotateLeft(grandparent);
                }
            }
        }

        root.color = Color.BLACK;
        root.parent = null;
    }

    private void rotateLeft(Node<K, V> pivot) {
        Node<K, V> promoted = pivot.right;
        if (promoted == null) {
            return;
        }

        pivot.right = promoted.left;
        if (promoted.left != null) {
            promoted.left.parent = pivot;
        }

        promoted.parent = pivot.parent;
        if (pivot.parent == null) {
            root = promoted;
        } else if (pivot == pivot.parent.left) {
            pivot.parent.left = promoted;
        } else {
            pivot.parent.right = promoted;
        }

        promoted.left = pivot;
        pivot.parent = promoted;
    }

    private void rotateRight(Node<K, V> pivot) {
        Node<K, V> promoted = pivot.left;
        if (promoted == null) {
            return;
        }

        pivot.left = promoted.right;
        if (promoted.right != null) {
            promoted.right.parent = pivot;
        }

        promoted.parent = pivot.parent;
        if (pivot.parent == null) {
            root = promoted;
        } else if (pivot == pivot.parent.left) {
            pivot.parent.left = promoted;
        } else {
            pivot.parent.right = promoted;
        }

        promoted.right = pivot;
        pivot.parent = promoted;
    }

    private Node<K, V> findNode(K key) {
        validateKey(key);

        Node<K, V> current = root;
        while (current != null) {
            int comparison = key.compareTo(current.key);
            if (comparison == 0) {
                return current;
            }

            current = comparison < 0 ? current.left : current.right;
        }

        return null;
    }

    private ValidationResult validateSubtree(Node<K, V> node, K lowerBound, K upperBound, int depth) {
        if (node == null) {
            return ValidationResult.valid(1, 0);
        }
        if (depth > size) {
            return ValidationResult.invalid();
        }
        if (node.color == null) {
            return ValidationResult.invalid();
        }
        if (lowerBound != null && node.key.compareTo(lowerBound) <= 0) {
            return ValidationResult.invalid();
        }
        if (upperBound != null && node.key.compareTo(upperBound) >= 0) {
            return ValidationResult.invalid();
        }
        if (node.left != null && node.left.parent != node) {
            return ValidationResult.invalid();
        }
        if (node.right != null && node.right.parent != node) {
            return ValidationResult.invalid();
        }
        if (node.color == Color.RED
                && (colorOf(node.left) == Color.RED || colorOf(node.right) == Color.RED)) {
            return ValidationResult.invalid();
        }

        ValidationResult left = validateSubtree(node.left, lowerBound, node.key, depth + 1);
        if (!left.valid) {
            return ValidationResult.invalid();
        }

        ValidationResult right = validateSubtree(node.right, node.key, upperBound, depth + 1);
        if (!right.valid || left.blackHeight != right.blackHeight) {
            return ValidationResult.invalid();
        }

        int blackHeight = left.blackHeight + (node.color == Color.BLACK ? 1 : 0);
        return ValidationResult.valid(blackHeight, 1 + left.nodeCount + right.nodeCount);
    }

    private void addInorder(Node<K, V> node, CustomDynamicArray<V> values) {
        if (node == null) {
            return;
        }

        addInorder(node.left, values);
        values.add(node.value);
        addInorder(node.right, values);
    }

    private void addPreorder(Node<K, V> node, CustomDynamicArray<V> values) {
        if (node == null) {
            return;
        }

        values.add(node.value);
        addPreorder(node.left, values);
        addPreorder(node.right, values);
    }

    private void addPostorder(Node<K, V> node, CustomDynamicArray<V> values) {
        if (node == null) {
            return;
        }

        addPostorder(node.left, values);
        addPostorder(node.right, values);
        values.add(node.value);
    }

    private int height(Node<K, V> node) {
        if (node == null) {
            return -1;
        }

        return 1 + Math.max(height(node.left), height(node.right));
    }

    private Color colorOf(Node<K, V> node) {
        return node == null ? Color.BLACK : node.color;
    }

    private Node<K, V> parentOf(Node<K, V> node) {
        return node == null ? null : node.parent;
    }

    private void validateKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null.");
        }
    }

    private void validateValue(V value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
    }

    private enum Color {
        RED,
        BLACK
    }

    private static final class Node<K, V> {
        private final K key;
        private V value;
        private Color color;
        private Node<K, V> left;
        private Node<K, V> right;
        private Node<K, V> parent;

        private Node(K key, V value, Color color) {
            this.key = key;
            this.value = value;
            this.color = color;
        }
    }

    private static final class ValidationResult {
        private final boolean valid;
        private final int blackHeight;
        private final int nodeCount;

        private ValidationResult(boolean valid, int blackHeight, int nodeCount) {
            this.valid = valid;
            this.blackHeight = blackHeight;
            this.nodeCount = nodeCount;
        }

        private static ValidationResult valid(int blackHeight, int nodeCount) {
            return new ValidationResult(true, blackHeight, nodeCount);
        }

        private static ValidationResult invalid() {
            return new ValidationResult(false, 0, 0);
        }
    }
}
