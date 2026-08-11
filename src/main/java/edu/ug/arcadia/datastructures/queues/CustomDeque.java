package edu.ug.arcadia.datastructures.queues;

import edu.ug.arcadia.datastructures.linear.CustomIterator;
import edu.ug.arcadia.datastructures.linear.CustomLinkedList;

public class CustomDeque<T> {
    private final CustomLinkedList<T> elements;

    public CustomDeque() {
        elements = new CustomLinkedList<>();
    }

    public void addFirst(T value) {
        elements.addFirst(value);
    }

    public void addLast(T value) {
        elements.addLast(value);
    }

    public T removeFirst() {
        return elements.removeFirst();
    }

    public T removeLast() {
        return elements.removeLast();
    }

    public T peekFirst() {
        return elements.peekFirst();
    }

    public T peekLast() {
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
