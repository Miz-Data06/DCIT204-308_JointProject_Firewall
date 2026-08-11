package edu.ug.arcadia.datastructures.queues;

import edu.ug.arcadia.datastructures.linear.CustomIterator;
import edu.ug.arcadia.datastructures.linear.CustomLinkedList;

public class CustomQueue<T> {
    private final CustomLinkedList<T> elements;

    public CustomQueue() {
        elements = new CustomLinkedList<>();
    }

    public void enqueue(T value) {
        elements.addLast(value);
    }

    public T dequeue() {
        return elements.removeFirst();
    }

    public T peek() {
        return elements.peekFirst();
    }

    public T peekRear() {
        return elements.peekLast();
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
