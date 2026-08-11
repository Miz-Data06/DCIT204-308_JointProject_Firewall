package com.fooddelivery.datastructures.linear;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

public class CustomDynamicArray<T> {
    private static final int DEFAULT_CAPACITY = 10;

    private T[] elements;
    private int size;

    public CustomDynamicArray() {
        this(DEFAULT_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public CustomDynamicArray(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Initial capacity must be greater than 0.");
        }

        elements = (T[]) new Object[initialCapacity];
        size = 0;
    }

    public void add(T value) {
        ensureCapacityForOneMore();
        elements[size] = value;
        size++;
    }

    public void insert(int index, T value) {
        validateInsertIndex(index);
        ensureCapacityForOneMore();

        for (int i = size; i > index; i--) {
            elements[i] = elements[i - 1];
        }

        elements[index] = value;
        size++;
    }

    public T get(int index) {
        validateElementIndex(index);
        return elements[index];
    }

    public T set(int index, T value) {
        validateElementIndex(index);

        T previousValue = elements[index];
        elements[index] = value;
        return previousValue;
    }

    public T remove(int index) {
        validateElementIndex(index);

        T removedValue = elements[index];
        for (int i = index; i < size - 1; i++) {
            elements[i] = elements[i + 1];
        }

        size--;
        elements[size] = null;
        return removedValue;
    }

    public boolean removeValue(T value) {
        int index = indexOf(value);
        if (index == -1) {
            return false;
        }

        remove(index);
        return true;
    }

    public int indexOf(T value) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(elements[i], value)) {
                return i;
            }
        }

        return -1;
    }

    public boolean contains(T value) {
        return indexOf(value) != -1;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return elements.length;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }

        size = 0;
    }

    public CustomIterator<T> iterator() {
        return new CustomIterator<>() {
            private int currentIndex;

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No more elements.");
                }

                T value = elements[currentIndex];
                currentIndex++;
                return value;
            }
        };
    }

    private void ensureCapacityForOneMore() {
        if (size == elements.length) {
            elements = Arrays.copyOf(elements, elements.length * 2);
        }
    }

    private void validateElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size + ".");
        }
    }

    private void validateInsertIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for insert size " + size + ".");
        }
    }
}
