package com.fooddelivery.datastructures.graphs;

import com.fooddelivery.datastructures.hashing.CustomMap;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;

/**
 * Generic custom disjoint-set structure with union by rank and path compression.
 *
 * @param <T> stored value type
 */
public class CustomDisjointSet<T> {
    private final CustomMap<T, Node<T>> nodes;
    private final CustomDynamicArray<Node<T>> nodeRecords;
    private int setCount;

    public CustomDisjointSet() {
        nodes = new CustomMap<>();
        nodeRecords = new CustomDynamicArray<>();
    }

    public CustomDisjointSet(int initialCapacity) {
        nodes = new CustomMap<>(initialCapacity);
        nodeRecords = new CustomDynamicArray<>();
    }

    /**
     * Registers a new singleton set in O(1) average time, excluding map resizing.
     *
     * @param value non-null unregistered value
     */
    public void makeSet(T value) {
        requireNonNull(value, "Value");
        if (nodes.containsKey(value)) {
            throw new IllegalArgumentException("Value is already registered");
        }

        Node<T> node = new Node<>(value);
        nodes.put(value, node);
        nodeRecords.add(node);
        setCount++;
    }

    /**
     * Returns the representative value in amortized O(alpha(n)) time.
     *
     * @param value non-null registered value
     * @return representative value stored in the root node
     */
    public T find(T value) {
        return findRoot(requireNode(value)).value;
    }

    /**
     * Unites two sets using union by rank in amortized O(alpha(n)) time.
     *
     * @param first non-null registered value
     * @param second non-null registered value
     * @return true when two separate sets were merged
     */
    public boolean union(T first, T second) {
        Node<T> firstRoot = findRoot(requireNode(first));
        Node<T> secondRoot = findRoot(requireNode(second));
        if (firstRoot == secondRoot) {
            return false;
        }

        if (firstRoot.rank < secondRoot.rank) {
            firstRoot.parent = secondRoot;
        } else if (firstRoot.rank > secondRoot.rank) {
            secondRoot.parent = firstRoot;
        } else {
            secondRoot.parent = firstRoot;
            firstRoot.rank++;
        }

        setCount--;
        return true;
    }

    /**
     * Checks whether two values share a representative in amortized O(alpha(n)) time.
     *
     * @param first non-null registered value
     * @param second non-null registered value
     * @return true when both values are in the same set
     */
    public boolean connected(T first, T second) {
        return findRoot(requireNode(first)) == findRoot(requireNode(second));
    }

    /**
     * Checks registration in O(1) average time and O(n) worst-case time.
     *
     * @param value non-null value
     * @return true when an equal value is registered
     */
    public boolean contains(T value) {
        requireNonNull(value, "Value");
        return nodes.containsKey(value);
    }

    public int size() {
        return nodes.size();
    }

    public int setCount() {
        return setCount;
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    /**
     * Clears all values while preserving underlying storage capacity.
     */
    public void clear() {
        nodes.clear();
        nodeRecords.clear();
        setCount = 0;
    }

    /**
     * Returns the immediate parent value without path compression.
     *
     * @param value non-null registered value
     * @return immediate parent value
     */
    public T parentOf(T value) {
        return requireNode(value).parent.value;
    }

    /**
     * Returns the stored rank without mutating the structure.
     *
     * @param value non-null registered value
     * @return stored rank
     */
    public int rankOf(T value) {
        return requireNode(value).rank;
    }

    /**
     * Returns parent-edge distance to the representative in O(h), bounded by size.
     *
     * @param value non-null registered value
     * @return number of parent links to the representative
     */
    public int depthOf(T value) {
        Node<T> node = requireNode(value);
        int depth = 0;
        int limit = size();
        while (node.parent != node) {
            depth++;
            if (depth > limit) {
                throw new IllegalStateException("Parent chain does not terminate");
            }
            node = node.parent;
        }
        return depth;
    }

    /**
     * Validates internal union-find invariants without path compression.
     *
     * @return true when the structure is internally consistent
     */
    public boolean isValidDisjointSet() {
        int size = size();
        if (setCount < 0 || setCount > size || nodeRecords.size() != size) {
            return false;
        }
        if (size == 0) {
            return setCount == 0 && nodeRecords.isEmpty() && nodes.isEmpty();
        }

        CustomDynamicArray<Node<T>> observedRoots = new CustomDynamicArray<>();
        for (int i = 0; i < nodeRecords.size(); i++) {
            Node<T> node = nodeRecords.get(i);
            if (node == null || node.value == null || node.parent == null || node.rank < 0) {
                return false;
            }
            if (nodes.get(node.value) != node) {
                return false;
            }
            if (hasEarlierIdentity(nodeRecords, node, i)) {
                return false;
            }

            RootSearchResult<T> result = findRootWithoutCompression(node);
            if (!result.valid) {
                return false;
            }
            if (!containsIdentity(observedRoots, result.root)) {
                observedRoots.add(result.root);
            }
        }

        return observedRoots.size() == setCount;
    }

    private Node<T> requireNode(T value) {
        requireNonNull(value, "Value");
        Node<T> node = nodes.get(value);
        if (node == null) {
            throw new IllegalArgumentException("Value is not registered");
        }
        return node;
    }

    private void requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }

    private Node<T> findRoot(Node<T> node) {
        if (node.parent == node) {
            return node;
        }
        node.parent = findRoot(node.parent);
        return node.parent;
    }

    private RootSearchResult<T> findRootWithoutCompression(Node<T> start) {
        Node<T> current = start;
        int steps = 0;
        int limit = size();
        while (current.parent != current) {
            if (current.parent.rank < current.rank) {
                return RootSearchResult.invalid();
            }
            Node<T> registeredParent = nodes.get(current.parent.value);
            if (registeredParent != current.parent) {
                return RootSearchResult.invalid();
            }
            steps++;
            if (steps > limit) {
                return RootSearchResult.invalid();
            }
            current = current.parent;
        }

        if (nodes.get(current.value) != current) {
            return RootSearchResult.invalid();
        }
        return RootSearchResult.valid(current);
    }

    private boolean hasEarlierIdentity(CustomDynamicArray<Node<T>> values, Node<T> target, int endExclusive) {
        for (int i = 0; i < endExclusive; i++) {
            if (values.get(i) == target) {
                return true;
            }
        }
        return false;
    }

    private boolean containsIdentity(CustomDynamicArray<Node<T>> values, Node<T> target) {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) == target) {
                return true;
            }
        }
        return false;
    }

    private static final class Node<T> {
        private final T value;
        private Node<T> parent;
        private int rank;

        private Node(T value) {
            this.value = value;
            parent = this;
        }
    }

    private static final class RootSearchResult<T> {
        private final boolean valid;
        private final Node<T> root;

        private RootSearchResult(boolean valid, Node<T> root) {
            this.valid = valid;
            this.root = root;
        }

        private static <T> RootSearchResult<T> valid(Node<T> root) {
            return new RootSearchResult<>(true, root);
        }

        private static <T> RootSearchResult<T> invalid() {
            return new RootSearchResult<>(false, null);
        }
    }
}
