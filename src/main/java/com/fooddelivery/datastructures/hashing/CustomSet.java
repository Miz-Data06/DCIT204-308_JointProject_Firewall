package com.fooddelivery.datastructures.hashing;

/**
 * Set abstraction backed by the project custom hash table.
 *
 * @param <T> element type
 */
public class CustomSet<T> {
    private static final Object PRESENT = new Object();

    private final CustomHashTable<T, Object> table;

    /**
     * Creates an empty set with the hash table default capacity.
     */
    public CustomSet() {
        table = new CustomHashTable<>();
    }

    /**
     * Creates an empty set with capacity handling delegated to CustomHashTable.
     *
     * @param initialCapacity positive requested capacity
     */
    public CustomSet(int initialCapacity) {
        table = new CustomHashTable<>(initialCapacity);
    }

    /**
     * Adds a value in O(1) average amortized time and O(n) worst-case time.
     *
     * @param value non-null value
     * @return true when the set changed
     */
    public boolean add(T value) {
        return table.put(value, PRESENT) == null;
    }

    /**
     * Checks membership in O(1) average time and O(n) worst-case time.
     *
     * @param value non-null value
     * @return true when present
     */
    public boolean contains(T value) {
        return table.containsKey(value);
    }

    /**
     * Removes a value in O(1) average time and O(n) worst-case time.
     *
     * @param value non-null value
     * @return true after successful removal
     */
    public boolean remove(T value) {
        table.remove(value);
        return true;
    }

    /**
     * Returns the number of elements in O(1) time.
     *
     * @return element count
     */
    public int size() {
        return table.size();
    }

    /**
     * Checks emptiness in O(1) time.
     *
     * @return true when empty
     */
    public boolean isEmpty() {
        return table.isEmpty();
    }

    /**
     * Clears the set in O(m) time, where m is bucket capacity.
     */
    public void clear() {
        table.clear();
    }

    /**
     * Returns the delegated hash-table capacity in O(1) time.
     *
     * @return current capacity
     */
    public int capacity() {
        return table.capacity();
    }

    /**
     * Returns the delegated load factor in O(1) time.
     *
     * @return size divided by capacity
     */
    public double loadFactor() {
        return table.loadFactor();
    }
}
