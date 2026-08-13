package com.fooddelivery.datastructures.queues;

import com.fooddelivery.datastructures.linear.CustomIterator;

import java.util.NoSuchElementException;

/**
 * Custom double-ended queue backed by private doubly linked nodes.
 *
 * <p>Values may be added, removed and inspected at either end in O(1) time.
 * Null values are rejected with {@link IllegalArgumentException}. Removing or
 * peeking from an empty deque throws {@link NoSuchElementException}.</p>
 */
public class CustomDeque<T> {
    private Node<T> front;
    private Node<T> rear;
    private int size;

    /**
     * Inserts a non-null value at the logical front in O(1) time.
     */
    public void addFront(T value) {
        requireNonNull(value);

        Node<T> newNode = new Node<>(value);
        if (isEmpty()) {
            front = newNode;
            rear = newNode;
        } else {
            newNode.next = front;
            front.previous = newNode;
            front = newNode;
        }

        size++;
    }

    /**
     * Inserts a non-null value at the logical rear in O(1) time.
     */
    public void addRear(T value) {
        requireNonNull(value);

        Node<T> newNode = new Node<>(value);
        if (isEmpty()) {
            front = newNode;
            rear = newNode;
        } else {
            newNode.previous = rear;
            rear.next = newNode;
            rear = newNode;
        }

        size++;
    }

    /**
     * Removes and returns the front value in O(1) time.
     */
    public T removeFront() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty.");
        }

        Node<T> removedNode = front;
        T removedValue = removedNode.value;
        front = front.next;
        size--;

        if (isEmpty()) {
            rear = null;
        } else {
            front.previous = null;
        }

        removedNode.next = null;
        removedNode.value = null;
        return removedValue;
    }

    /**
     * Removes and returns the rear value in O(1) time.
     */
    public T removeRear() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty.");
        }

        Node<T> removedNode = rear;
        T removedValue = removedNode.value;
        rear = rear.previous;
        size--;

        if (isEmpty()) {
            front = null;
        } else {
            rear.next = null;
        }

        removedNode.previous = null;
        removedNode.value = null;
        return removedValue;
    }

    /**
     * Returns the front value without removing it in O(1) time.
     */
    public T peekFront() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty.");
        }

        return front.value;
    }

    /**
     * Returns the rear value without removing it in O(1) time.
     */
    public T peekRear() {
        if (isEmpty()) {
            throw new NoSuchElementException("Deque is empty.");
        }

        return rear.value;
    }

    /**
     * Returns the number of stored values in O(1) time.
     */
    public int size() {
        return size;
    }

    /**
     * Returns true exactly when the deque contains no values.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all values and endpoint references.
     */
    public void clear() {
        Node<T> currentNode = front;
        while (currentNode != null) {
            Node<T> nextNode = currentNode.next;
            currentNode.previous = null;
            currentNode.next = null;
            currentNode.value = null;
            currentNode = nextNode;
        }

        front = null;
        rear = null;
        size = 0;
    }

    /**
     * Returns an iterator that visits values from front to rear.
     */
    public CustomIterator<T> iterator() {
        return new CustomIterator<>() {
            private Node<T> currentNode = front;

            @Override
            public boolean hasNext() {
                return currentNode != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No more elements.");
                }

                T value = currentNode.value;
                currentNode = currentNode.next;
                return value;
            }
        };
    }

    /**
     * @deprecated Use {@link #addFront(Object)}.
     */
    @Deprecated
    public void addFirst(T value) {
        addFront(value);
    }

    /**
     * @deprecated Use {@link #addRear(Object)}.
     */
    @Deprecated
    public void addLast(T value) {
        addRear(value);
    }

    /**
     * @deprecated Use {@link #removeFront()}.
     */
    @Deprecated
    public T removeFirst() {
        return removeFront();
    }

    /**
     * @deprecated Use {@link #removeRear()}.
     */
    @Deprecated
    public T removeLast() {
        return removeRear();
    }

    /**
     * @deprecated Use {@link #peekFront()}.
     */
    @Deprecated
    public T peekFirst() {
        return peekFront();
    }

    /**
     * @deprecated Use {@link #peekRear()}.
     */
    @Deprecated
    public T peekLast() {
        return peekRear();
    }

    private void requireNonNull(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Deque value cannot be null.");
        }
    }

    private static class Node<T> {
        private T value;
        private Node<T> previous;
        private Node<T> next;

        private Node(T value) {
            this.value = value;
        }
    }
}
