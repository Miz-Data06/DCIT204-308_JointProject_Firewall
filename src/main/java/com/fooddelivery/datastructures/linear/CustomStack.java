package com.fooddelivery.datastructures.linear;

public class CustomStack<T> {
    private final CustomLinkedList<T> elements;

    public CustomStack() {
        elements = new CustomLinkedList<>();
    }

    public void push(T value) {
        elements.addFirst(value);
    }

    public T pop() {
        return elements.removeFirst();
    }

    public T peek() {
        return elements.peekFirst();
    }

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public void clear() {
        elements.clear();
    }

    public CustomIterator<T> iterator() {
        return elements.iterator();
    }
}
