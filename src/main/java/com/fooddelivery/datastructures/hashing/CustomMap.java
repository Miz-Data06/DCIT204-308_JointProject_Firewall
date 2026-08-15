package com.fooddelivery.datastructures.hashing;

/**
 * Map abstraction backed by the project custom hash table.
 *
 * @param <K> key type
 * @param <V> value type
 */
public class CustomMap<K, V> {
    private final CustomHashTable<K, V> table;

    /**
     * Creates an empty map with the hash table default capacity.
     */
    public CustomMap() {
        table = new CustomHashTable<>();
    }

    /**
     * Creates an empty map with capacity handling delegated to CustomHashTable.
     *
     * @param initialCapacity positive requested capacity
     */
    public CustomMap(int initialCapacity) {
        table = new CustomHashTable<>(initialCapacity);
    }

    /**
     * Adds or updates a mapping in O(1) average amortized time and O(n) worst-case time.
     *
     * @param key non-null key
     * @param value non-null value
     * @return previous value, or null for a new key
     */
    public V put(K key, V value) {
        return table.put(key, value);
    }

    /**
     * Looks up a key in O(1) average time and O(n) worst-case time.
     *
     * @param key non-null key
     * @return stored value, or null when absent
     */
    public V get(K key) {
        return table.get(key);
    }

    /**
     * Removes a mapping in O(1) average time and O(n) worst-case time.
     *
     * @param key non-null key
     * @return removed value
     */
    public V remove(K key) {
        return table.remove(key);
    }

    /**
     * Checks key presence in O(1) average time and O(n) worst-case time.
     *
     * @param key non-null key
     * @return true when present
     */
    public boolean containsKey(K key) {
        return table.containsKey(key);
    }

    /**
     * Returns the number of mappings in O(1) time.
     *
     * @return mapping count
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
     * Clears the map in O(m) time, where m is bucket capacity.
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
