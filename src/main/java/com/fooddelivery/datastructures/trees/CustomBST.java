package com.fooddelivery.datastructures.trees;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;

/**
 * Generic binary search tree for key-value indexing.
 *
 * <p>Keys are ordered only by {@link Comparable#compareTo(Object)}. Inserting a
 * key already present in the tree replaces the stored value without creating a
 * duplicate node or changing the size. Missing-key searches return {@code null},
 * while missing-key removals throw {@link IllegalArgumentException}.</p>
 *
 * <p>Traversal methods return independent {@link CustomDynamicArray} instances
 * containing values, not keys or internal nodes. This ordinary BST is not
 * self-balancing, so operations that run in O(h) can degrade to O(n) when the
 * tree becomes skewed. Height is edge-based: an empty tree has height -1 and a
 * one-node tree has height 0.</p>
 *
 * @param <K> comparable key type
 * @param <V> stored value type
 */
public class CustomBST<K extends Comparable<? super K>, V> {
    private Node<K, V> root;
    private int size;

    /**
     * Inserts a key-value entry in O(h), replacing the value when the key exists.
     *
     * @param key comparable key
     * @param value non-null value
     * @throws IllegalArgumentException if key or value is null
     */
    public void insert(K key, V value) {
        validateKey(key);
        validateValue(value);

        if (root == null) {
            root = new Node<>(key, value);
            size = 1;
            return;
        }

        Node<K, V> current = root;
        while (true) {
            int comparison = key.compareTo(current.key);
            if (comparison == 0) {
                current.value = value;
                return;
            }

            if (comparison < 0) {
                if (current.left == null) {
                    current.left = new Node<>(key, value);
                    size++;
                    return;
                }
                current = current.left;
            } else {
                if (current.right == null) {
                    current.right = new Node<>(key, value);
                    size++;
                    return;
                }
                current = current.right;
            }
        }
    }

    /**
     * Searches for a key in O(h).
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
     * Returns whether a key exists in O(h).
     *
     * @param key key to find
     * @return true when present
     * @throws IllegalArgumentException if key is null
     */
    public boolean containsKey(K key) {
        return findNode(key) != null;
    }

    /**
     * Removes a key in O(h) and returns the value originally stored for it.
     *
     * @param key key to remove
     * @return value associated with the removed key
     * @throws IllegalArgumentException if key is null or absent
     */
    public V remove(K key) {
        validateKey(key);

        Node<K, V> parent = null;
        Node<K, V> current = root;
        while (current != null) {
            int comparison = key.compareTo(current.key);
            if (comparison == 0) {
                break;
            }

            parent = current;
            current = comparison < 0 ? current.left : current.right;
        }

        if (current == null) {
            throw new IllegalArgumentException("Key is not present.");
        }

        V removedValue = current.value;
        if (current.left != null && current.right != null) {
            Node<K, V> successorParent = current;
            Node<K, V> successor = current.right;
            while (successor.left != null) {
                successorParent = successor;
                successor = successor.left;
            }

            current.key = successor.key;
            current.value = successor.value;
            parent = successorParent;
            current = successor;
        }

        Node<K, V> child = current.left != null ? current.left : current.right;
        if (parent == null) {
            root = child;
        } else if (parent.left == current) {
            parent.left = child;
        } else {
            parent.right = child;
        }

        size--;
        return removedValue;
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

        int leftHeight = height(node.left);
        int rightHeight = height(node.right);
        return 1 + Math.max(leftHeight, rightHeight);
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

    private static final class Node<K, V> {
        private K key;
        private V value;
        private Node<K, V> left;
        private Node<K, V> right;

        private Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
