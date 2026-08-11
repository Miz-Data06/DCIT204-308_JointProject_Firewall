package edu.ug.arcadia.datastructures.linear;

import java.util.NoSuchElementException;
import java.util.Objects;

public class CustomLinkedList<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    public void addFirst(T value) {
        Node<T> newNode = new Node<>(value);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            newNode.next = head;
            head = newNode;
        }

        size++;
    }

    public void addLast(T value) {
        Node<T> newNode = new Node<>(value);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }

        size++;
    }

    public void insert(int index, T value) {
        validateInsertIndex(index);

        if (index == 0) {
            addFirst(value);
            return;
        }

        if (index == size) {
            addLast(value);
            return;
        }

        Node<T> previousNode = nodeAt(index - 1);
        Node<T> newNode = new Node<>(value);
        newNode.next = previousNode.next;
        previousNode.next = newNode;
        size++;
    }

    public T get(int index) {
        validateElementIndex(index);
        return nodeAt(index).value;
    }

    public T set(int index, T value) {
        validateElementIndex(index);

        Node<T> node = nodeAt(index);
        T previousValue = node.value;
        node.value = value;
        return previousValue;
    }

    public T removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty.");
        }

        T removedValue = head.value;
        head = head.next;
        size--;

        if (size == 0) {
            tail = null;
        }

        return removedValue;
    }

    public T removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty.");
        }

        if (size == 1) {
            return removeFirst();
        }

        Node<T> previousNode = nodeAt(size - 2);
        T removedValue = tail.value;
        previousNode.next = null;
        tail = previousNode;
        size--;
        return removedValue;
    }

    public T remove(int index) {
        validateElementIndex(index);

        if (index == 0) {
            return removeFirst();
        }

        if (index == size - 1) {
            return removeLast();
        }

        Node<T> previousNode = nodeAt(index - 1);
        Node<T> removedNode = previousNode.next;
        previousNode.next = removedNode.next;
        size--;
        return removedNode.value;
    }

    public boolean removeValue(T value) {
        if (isEmpty()) {
            return false;
        }

        if (Objects.equals(head.value, value)) {
            removeFirst();
            return true;
        }

        Node<T> previousNode = head;
        Node<T> currentNode = head.next;
        while (currentNode != null) {
            if (Objects.equals(currentNode.value, value)) {
                previousNode.next = currentNode.next;
                if (currentNode == tail) {
                    tail = previousNode;
                }

                size--;
                return true;
            }

            previousNode = currentNode;
            currentNode = currentNode.next;
        }

        return false;
    }

    public int indexOf(T value) {
        Node<T> currentNode = head;
        int index = 0;
        while (currentNode != null) {
            if (Objects.equals(currentNode.value, value)) {
                return index;
            }

            currentNode = currentNode.next;
            index++;
        }

        return -1;
    }

    public boolean contains(T value) {
        return indexOf(value) != -1;
    }

    public T peekFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty.");
        }

        return head.value;
    }

    public T peekLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty.");
        }

        return tail.value;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        Node<T> currentNode = head;
        while (currentNode != null) {
            Node<T> nextNode = currentNode.next;
            currentNode.next = null;
            currentNode = nextNode;
        }

        head = null;
        tail = null;
        size = 0;
    }

    public CustomIterator<T> iterator() {
        return new CustomIterator<>() {
            private Node<T> currentNode = head;

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

    private Node<T> nodeAt(int index) {
        Node<T> currentNode = head;
        for (int i = 0; i < index; i++) {
            currentNode = currentNode.next;
        }

        return currentNode;
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

    private static class Node<T> {
        private T value;
        private Node<T> next;

        private Node(T value) {
            this.value = value;
        }
    }
}
