package edu.ug.arcadia.datastructures.heaps;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

public class PriorityHeap<T extends Comparable<? super T>> {
    private static final int DEFAULT_CAPACITY = 10;

    private T[] elements;
    private int size;

    public PriorityHeap() {
        this(DEFAULT_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public PriorityHeap(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Initial capacity must be greater than 0.");
        }

        elements = (T[]) new Comparable[initialCapacity];
        size = 0;
    }

    public void add(T value) {
        Objects.requireNonNull(value, "Value cannot be null.");
        ensureCapacityForOneMore();

        elements[size] = value;
        siftUp(size);
        size++;
    }

    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty.");
        }

        return elements[0];
    }

    public T remove() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty.");
        }

        T removedValue = elements[0];
        size--;

        if (size == 0) {
            elements[0] = null;
            return removedValue;
        }

        elements[0] = elements[size];
        elements[size] = null;
        siftDown(0);
        return removedValue;
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

    private void siftUp(int index) {
        int childIndex = index;
        while (childIndex > 0) {
            int parentIndex = (childIndex - 1) / 2;
            if (elements[childIndex].compareTo(elements[parentIndex]) >= 0) {
                return;
            }

            swap(childIndex, parentIndex);
            childIndex = parentIndex;
        }
    }

    private void siftDown(int index) {
        int parentIndex = index;
        while (true) {
            int leftChildIndex = (2 * parentIndex) + 1;
            int rightChildIndex = (2 * parentIndex) + 2;
            int smallerChildIndex = parentIndex;

            if (leftChildIndex < size
                    && elements[leftChildIndex].compareTo(elements[smallerChildIndex]) < 0) {
                smallerChildIndex = leftChildIndex;
            }

            if (rightChildIndex < size
                    && elements[rightChildIndex].compareTo(elements[smallerChildIndex]) < 0) {
                smallerChildIndex = rightChildIndex;
            }

            if (smallerChildIndex == parentIndex) {
                return;
            }

            swap(parentIndex, smallerChildIndex);
            parentIndex = smallerChildIndex;
        }
    }

    private void ensureCapacityForOneMore() {
        if (size < elements.length) {
            return;
        }

        int newCapacity = calculateNewCapacity();
        elements = Arrays.copyOf(elements, newCapacity);
    }

    private int calculateNewCapacity() {
        if (elements.length > Integer.MAX_VALUE / 2) {
            if (elements.length == Integer.MAX_VALUE) {
                throw new OutOfMemoryError("Heap backing array cannot grow further.");
            }

            return Integer.MAX_VALUE;
        }

        return elements.length * 2;
    }

    private void swap(int firstIndex, int secondIndex) {
        T temporary = elements[firstIndex];
        elements[firstIndex] = elements[secondIndex];
        elements[secondIndex] = temporary;
    }
}
