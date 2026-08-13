package com.fooddelivery.datastructures.hashing;

/**
 * Generic separate-chaining hash table with fixed load-factor resizing policy.
 *
 * @param <K> key type
 * @param <V> value type
 */
public class CustomHashTable<K, V> {
    private static final int DEFAULT_CAPACITY = 11;
    private static final double MAX_LOAD_FACTOR = 0.75;

    private Entry<K, V>[] buckets;
    private int size;

    /**
     * Creates an empty table with deterministic prime capacity 11.
     */
    public CustomHashTable() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates an empty table using the requested capacity normalized upward to
     * the nearest prime capacity.
     *
     * @param initialCapacity positive requested capacity
     */
    public CustomHashTable(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Initial capacity must be greater than zero");
        }
        buckets = createBucketArray(nextPrimeAtLeast(initialCapacity));
        size = 0;
    }

    /**
     * Stores a mapping in O(1) average amortized time and O(n) worst-case time.
     * Duplicate keys replace the previous value without resizing.
     *
     * @param key non-null key
     * @param value non-null value
     * @return previous value, or null when the key was absent
     */
    public V put(K key, V value) {
        validateKey(key);
        validateValue(value);

        Entry<K, V> existing = findEntry(key);
        if (existing != null) {
            V previousValue = existing.value;
            existing.value = value;
            return previousValue;
        }

        if (wouldExceedLoadFactor()) {
            resize();
        }

        int index = bucketIndex(key, buckets.length);
        buckets[index] = new Entry<>(key, value, buckets[index]);
        size++;
        return null;
    }

    /**
     * Retrieves a value in O(1) average time and O(n) worst-case time.
     *
     * @param key non-null key
     * @return stored value, or null when absent
     */
    public V get(K key) {
        validateKey(key);
        Entry<K, V> entry = findEntry(key);
        return entry == null ? null : entry.value;
    }

    /**
     * Removes a mapping in O(1) average time and O(n) worst-case time.
     *
     * @param key non-null key
     * @return removed value
     */
    public V remove(K key) {
        validateKey(key);

        int index = bucketIndex(key, buckets.length);
        Entry<K, V> previous = null;
        Entry<K, V> current = buckets[index];
        while (current != null) {
            if (key.equals(current.key)) {
                if (previous == null) {
                    buckets[index] = current.next;
                } else {
                    previous.next = current.next;
                }

                V removedValue = current.value;
                current.next = null;
                size--;
                return removedValue;
            }

            previous = current;
            current = current.next;
        }

        throw new IllegalArgumentException("Key does not exist");
    }

    /**
     * Checks key presence in O(1) average time and O(n) worst-case time.
     *
     * @param key non-null key
     * @return true when the key exists
     */
    public boolean containsKey(K key) {
        validateKey(key);
        return findEntry(key) != null;
    }

    /**
     * Returns the number of mappings in O(1) time.
     *
     * @return mapping count
     */
    public int size() {
        return size;
    }

    /**
     * Checks emptiness in O(1) time.
     *
     * @return true when no mappings exist
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns bucket-array capacity in O(1) time.
     *
     * @return current capacity
     */
    public int capacity() {
        return buckets.length;
    }

    /**
     * Returns the current load factor in O(1) time.
     *
     * @return size divided by current capacity
     */
    public double loadFactor() {
        return (double) size / buckets.length;
    }

    /**
     * Clears all mappings in O(m) time, where m is current capacity.
     */
    public void clear() {
        for (int i = 0; i < buckets.length; i++) {
            Entry<K, V> current = buckets[i];
            while (current != null) {
                Entry<K, V> next = current.next;
                current.next = null;
                current = next;
            }
            buckets[i] = null;
        }
        size = 0;
    }

    private Entry<K, V> findEntry(K key) {
        int index = bucketIndex(key, buckets.length);
        Entry<K, V> current = buckets[index];
        while (current != null) {
            if (key.equals(current.key)) {
                return current;
            }
            current = current.next;
        }
        return null;
    }

    private boolean wouldExceedLoadFactor() {
        return (size + 1.0) / buckets.length > MAX_LOAD_FACTOR;
    }

    private void resize() {
        int newCapacity = nextPrimeGreaterThanTwice(buckets.length);
        Entry<K, V>[] newBuckets = createBucketArray(newCapacity);

        for (Entry<K, V> bucket : buckets) {
            Entry<K, V> current = bucket;
            while (current != null) {
                Entry<K, V> next = current.next;
                int newIndex = bucketIndex(current.key, newCapacity);
                current.next = newBuckets[newIndex];
                newBuckets[newIndex] = current;
                current = next;
            }
        }

        buckets = newBuckets;
    }

    private int bucketIndex(K key, int capacity) {
        int hash = key.hashCode();
        int spread = hash ^ (hash >>> 16);
        return Math.floorMod(spread, capacity);
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

    private static int nextPrimeGreaterThanTwice(int currentCapacity) {
        long doubled = (long) currentCapacity * 2L;
        if (doubled >= Integer.MAX_VALUE) {
            throw new IllegalStateException("Hash table capacity cannot grow further");
        }

        long candidate = doubled + 1L;
        while (candidate <= Integer.MAX_VALUE) {
            int intCandidate = (int) candidate;
            if (isPrime(intCandidate)) {
                return intCandidate;
            }
            candidate += intCandidate == 2 ? 1L : 2L;
            if (candidate > Integer.MAX_VALUE && candidate - 1L <= Integer.MAX_VALUE) {
                candidate = Integer.MAX_VALUE;
            }
        }

        throw new IllegalStateException("Hash table capacity cannot grow further");
    }

    private static int nextPrimeAtLeast(int value) {
        if (value <= 2) {
            return 2;
        }

        int candidate = value % 2 == 0 ? value + 1 : value;
        while (candidate > 0) {
            if (isPrime(candidate)) {
                return candidate;
            }
            if (candidate > Integer.MAX_VALUE - 2) {
                if (candidate != Integer.MAX_VALUE && isPrime(Integer.MAX_VALUE)) {
                    return Integer.MAX_VALUE;
                }
                throw new IllegalStateException("No supported prime capacity exists");
            }
            candidate += 2;
        }

        throw new IllegalStateException("No supported prime capacity exists");
    }

    private static boolean isPrime(int value) {
        if (value < 2) {
            return false;
        }
        if (value == 2) {
            return true;
        }
        if (value % 2 == 0) {
            return false;
        }

        for (int divisor = 3; divisor <= value / divisor; divisor += 2) {
            if (value % divisor == 0) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private Entry<K, V>[] createBucketArray(int capacity) {
        return (Entry<K, V>[]) new Entry[capacity];
    }

    private static final class Entry<K, V> {
        private final K key;
        private V value;
        private Entry<K, V> next;

        private Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
}
