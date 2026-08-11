package com.fooddelivery.datastructures.linear;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomStackTest {
    @Test
    void newStackIsEmptyWithSizeZero() {
        CustomStack<String> stack = new CustomStack<>();

        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    @Test
    void pushIncreasesSize() {
        CustomStack<String> stack = new CustomStack<>();

        stack.push("first");
        stack.push("second");

        assertEquals(2, stack.size());
        assertFalse(stack.isEmpty());
    }

    @Test
    void peekReturnsMostRecentlyPushedValue() {
        CustomStack<String> stack = new CustomStack<>();

        stack.push("first");
        stack.push("second");

        assertEquals("second", stack.peek());
    }

    @Test
    void peekDoesNotRemoveValue() {
        CustomStack<String> stack = new CustomStack<>();
        stack.push("top");

        assertEquals("top", stack.peek());
        assertEquals("top", stack.peek());
        assertEquals(1, stack.size());
    }

    @Test
    void popReturnsValuesInLifoOrder() {
        CustomStack<String> stack = new CustomStack<>();
        stack.push("first");
        stack.push("second");
        stack.push("third");

        assertEquals("third", stack.pop());
        assertEquals("second", stack.pop());
        assertEquals("first", stack.pop());
    }

    @Test
    void popDecreasesSize() {
        CustomStack<String> stack = new CustomStack<>();
        stack.push("first");
        stack.push("second");

        assertEquals("second", stack.pop());

        assertEquals(1, stack.size());
        assertEquals("first", stack.peek());
    }

    @Test
    void sequenceOfPushesAndPopsBehavesCorrectly() {
        CustomStack<String> stack = new CustomStack<>();

        stack.push("a");
        stack.push("b");
        assertEquals("b", stack.pop());
        stack.push("c");
        stack.push("d");

        assertEquals("d", stack.pop());
        assertEquals("c", stack.pop());
        assertEquals("a", stack.pop());
        assertTrue(stack.isEmpty());
    }

    @Test
    void popOnEmptyStackThrowsNoSuchElementException() {
        CustomStack<String> stack = new CustomStack<>();

        assertThrows(NoSuchElementException.class, stack::pop);
    }

    @Test
    void peekOnEmptyStackThrowsNoSuchElementException() {
        CustomStack<String> stack = new CustomStack<>();

        assertThrows(NoSuchElementException.class, stack::peek);
    }

    @Test
    void stackCanContainNull() {
        CustomStack<String> stack = new CustomStack<>();

        stack.push(null);

        assertFalse(stack.isEmpty());
        assertEquals(1, stack.size());
    }

    @Test
    void nullValueCanBePushedPeekedAndPopped() {
        CustomStack<String> stack = new CustomStack<>();

        stack.push("below");
        stack.push(null);

        assertEquals(null, stack.peek());
        assertEquals(null, stack.pop());
        assertEquals("below", stack.peek());
    }

    @Test
    void duplicateValuesAreTreatedAsSeparateEntries() {
        CustomStack<String> stack = new CustomStack<>();

        stack.push("same");
        stack.push("same");

        assertEquals(2, stack.size());
        assertEquals("same", stack.pop());
        assertEquals(1, stack.size());
        assertEquals("same", stack.pop());
        assertTrue(stack.isEmpty());
    }

    @Test
    void clearEmptiesStack() {
        CustomStack<String> stack = new CustomStack<>();
        stack.push("first");
        stack.push("second");

        stack.clear();

        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
        assertThrows(NoSuchElementException.class, stack::peek);
    }

    @Test
    void stackCanBeReusedAfterClear() {
        CustomStack<String> stack = new CustomStack<>();
        stack.push("old");
        stack.clear();

        stack.push("new");

        assertEquals(1, stack.size());
        assertEquals("new", stack.peek());
    }

    @Test
    void iteratorTraversesFromTopToBottom() {
        CustomStack<String> stack = new CustomStack<>();
        stack.push("bottom");
        stack.push("middle");
        stack.push("top");

        CustomIterator<String> iterator = stack.iterator();

        assertTrue(iterator.hasNext());
        assertEquals("top", iterator.next());
        assertEquals("middle", iterator.next());
        assertEquals("bottom", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorDoesNotChangeStack() {
        CustomStack<String> stack = new CustomStack<>();
        stack.push("bottom");
        stack.push("top");
        CustomIterator<String> iterator = stack.iterator();

        assertEquals("top", iterator.next());
        assertEquals("bottom", iterator.next());

        assertEquals(2, stack.size());
        assertEquals("top", stack.peek());
    }

    @Test
    void iteratorWorksOnEmptyStack() {
        CustomStack<String> stack = new CustomStack<>();
        CustomIterator<String> iterator = stack.iterator();

        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorNextAfterExhaustionThrowsNoSuchElementException() {
        CustomStack<String> stack = new CustomStack<>();
        stack.push("only");
        CustomIterator<String> iterator = stack.iterator();

        assertEquals("only", iterator.next());

        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void oneElementStackReturnsToEmptyAfterPop() {
        CustomStack<String> stack = new CustomStack<>();
        stack.push("only");

        assertEquals("only", stack.pop());

        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
        assertThrows(NoSuchElementException.class, stack::pop);
    }

    @Test
    void interleavedOperationsPreserveTopValueAndSize() {
        CustomStack<String> stack = new CustomStack<>();

        stack.push("a");
        stack.push("b");
        assertEquals("b", stack.peek());
        assertEquals(2, stack.size());

        assertEquals("b", stack.pop());
        assertEquals("a", stack.peek());
        assertEquals(1, stack.size());

        stack.push("c");
        assertEquals("c", stack.peek());
        assertEquals(2, stack.size());

        assertEquals("c", stack.pop());
        assertEquals("a", stack.pop());
        assertTrue(stack.isEmpty());
    }
}
