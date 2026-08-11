package com.fooddelivery.datastructures.queues;

import com.fooddelivery.datastructures.linear.CustomIterator;

import java.util.NoSuchElementException;

public class CustomCircularQueue<T> {
    private T[] elements;
    private int front;
    private int rear;
    private int size;

    @SuppressWarnings("unchecked")
    public CustomCircularQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0.");
        }

        elements = (T[]) new Object[capacity];
        front = 0;
        rear = 0;
        size = 0;
    }

    public void enqueue(T value) {
        if (isFull()) {
            throw new IllegalStateException("Queue is full.");
        }

        elements[rear] = value;
        rear = (rear + 1) % elements.length;
        size++;
    }

    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty.");
        }

        T removedValue = elements[front];
        elements[front] = null;
        front = (front + 1) % elements.length;
        size--;
        return removedValue;
    }

    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty.");
        }

        return elements[front];
    }

    public T peekRear() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty.");
        }

        int rearIndex = (rear - 1 + elements.length) % elements.length;
        return elements[rearIndex];
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

    public boolean isFull() {
        return size == elements.length;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            int index = (front + i) % elements.length;
            elements[index] = null;
        }

        front = 0;
        rear = 0;
        size = 0;
    }

    public CustomIterator<T> iterator() {
        return new CustomIterator<>() {
            private int visitedCount;

            @Override
            public boolean hasNext() {
                return visitedCount < size;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No more elements.");
                }

                int index = (front + visitedCount) % elements.length;
                T value = elements[index];
                visitedCount++;
                return value;
            }
        };
    }
}
