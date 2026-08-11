package edu.ug.arcadia.datastructures.queues;

import edu.ug.arcadia.datastructures.linear.CustomIterator;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomQueueTest {
    @Test
    void newQueueIsEmptyWithSizeZero() {
        CustomQueue<String> queue = new CustomQueue<>();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void enqueueIncreasesSize() {
        CustomQueue<String> queue = new CustomQueue<>();

        queue.enqueue("first");
        queue.enqueue("second");

        assertEquals(2, queue.size());
        assertFalse(queue.isEmpty());
    }

    @Test
    void peekReturnsEarliestEnqueuedValue() {
        CustomQueue<String> queue = new CustomQueue<>();

        queue.enqueue("first");
        queue.enqueue("second");

        assertEquals("first", queue.peek());
    }

    @Test
    void peekDoesNotRemoveValue() {
        CustomQueue<String> queue = new CustomQueue<>();
        queue.enqueue("first");

        assertEquals("first", queue.peek());
        assertEquals("first", queue.peek());
        assertEquals(1, queue.size());
    }

    @Test
    void peekRearReturnsLatestEnqueuedValue() {
        CustomQueue<String> queue = new CustomQueue<>();

        queue.enqueue("first");
        queue.enqueue("second");

        assertEquals("second", queue.peekRear());
    }

    @Test
    void peekRearDoesNotRemoveValue() {
        CustomQueue<String> queue = new CustomQueue<>();
        queue.enqueue("first");
        queue.enqueue("second");

        assertEquals("second", queue.peekRear());
        assertEquals("second", queue.peekRear());
        assertEquals(2, queue.size());
    }

    @Test
    void dequeueFollowsFifoOrder() {
        CustomQueue<String> queue = new CustomQueue<>();
        queue.enqueue("first");
        queue.enqueue("second");
        queue.enqueue("third");

        assertEquals("first", queue.dequeue());
        assertEquals("second", queue.dequeue());
        assertEquals("third", queue.dequeue());
    }

    @Test
    void dequeueDecreasesSize() {
        CustomQueue<String> queue = new CustomQueue<>();
        queue.enqueue("first");
        queue.enqueue("second");

        assertEquals("first", queue.dequeue());

        assertEquals(1, queue.size());
        assertEquals("second", queue.peek());
    }

    @Test
    void interleavedEnqueueAndDequeueOperationsPreserveFifoOrder() {
        CustomQueue<String> queue = new CustomQueue<>();

        queue.enqueue("a");
        queue.enqueue("b");
        assertEquals("a", queue.dequeue());
        queue.enqueue("c");
        queue.enqueue("d");

        assertEquals("b", queue.dequeue());
        assertEquals("c", queue.dequeue());
        assertEquals("d", queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    void dequeueOnEmptyQueueThrowsNoSuchElementException() {
        CustomQueue<String> queue = new CustomQueue<>();

        assertThrows(NoSuchElementException.class, queue::dequeue);
    }

    @Test
    void peekOnEmptyQueueThrowsNoSuchElementException() {
        CustomQueue<String> queue = new CustomQueue<>();

        assertThrows(NoSuchElementException.class, queue::peek);
    }

    @Test
    void peekRearOnEmptyQueueThrowsNoSuchElementException() {
        CustomQueue<String> queue = new CustomQueue<>();

        assertThrows(NoSuchElementException.class, queue::peekRear);
    }

    @Test
    void queueCanContainNull() {
        CustomQueue<String> queue = new CustomQueue<>();

        queue.enqueue(null);

        assertFalse(queue.isEmpty());
        assertEquals(1, queue.size());
    }

    @Test
    void nullValueCanBeEnqueuedPeekedAndDequeued() {
        CustomQueue<String> queue = new CustomQueue<>();

        queue.enqueue(null);
        queue.enqueue("after");

        assertEquals(null, queue.peek());
        assertEquals(null, queue.dequeue());
        assertEquals("after", queue.peek());
    }

    @Test
    void duplicateValuesRemainSeparateQueueEntries() {
        CustomQueue<String> queue = new CustomQueue<>();

        queue.enqueue("same");
        queue.enqueue("same");

        assertEquals(2, queue.size());
        assertEquals("same", queue.dequeue());
        assertEquals(1, queue.size());
        assertEquals("same", queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    void clearEmptiesQueue() {
        CustomQueue<String> queue = new CustomQueue<>();
        queue.enqueue("first");
        queue.enqueue("second");

        queue.clear();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertThrows(NoSuchElementException.class, queue::peek);
    }

    @Test
    void queueCanBeReusedAfterClear() {
        CustomQueue<String> queue = new CustomQueue<>();
        queue.enqueue("old");
        queue.clear();

        queue.enqueue("new");

        assertEquals(1, queue.size());
        assertEquals("new", queue.peek());
        assertEquals("new", queue.peekRear());
    }

    @Test
    void iteratorTraversesFromFrontToRear() {
        CustomQueue<String> queue = new CustomQueue<>();
        queue.enqueue("front");
        queue.enqueue("middle");
        queue.enqueue("rear");

        CustomIterator<String> iterator = queue.iterator();

        assertTrue(iterator.hasNext());
        assertEquals("front", iterator.next());
        assertEquals("middle", iterator.next());
        assertEquals("rear", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorDoesNotChangeQueue() {
        CustomQueue<String> queue = new CustomQueue<>();
        queue.enqueue("front");
        queue.enqueue("rear");
        CustomIterator<String> iterator = queue.iterator();

        assertEquals("front", iterator.next());
        assertEquals("rear", iterator.next());

        assertEquals(2, queue.size());
        assertEquals("front", queue.peek());
        assertEquals("rear", queue.peekRear());
    }

    @Test
    void iteratorWorksOnEmptyQueue() {
        CustomQueue<String> queue = new CustomQueue<>();
        CustomIterator<String> iterator = queue.iterator();

        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorNextAfterExhaustionThrowsNoSuchElementException() {
        CustomQueue<String> queue = new CustomQueue<>();
        queue.enqueue("only");
        CustomIterator<String> iterator = queue.iterator();

        assertEquals("only", iterator.next());

        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void oneElementQueueBecomesEmptyAfterDequeue() {
        CustomQueue<String> queue = new CustomQueue<>();
        queue.enqueue("only");

        assertEquals("only", queue.dequeue());

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertThrows(NoSuchElementException.class, queue::dequeue);
    }

    @Test
    void frontAndRearRemainCorrectThroughoutMixedOperations() {
        CustomQueue<String> queue = new CustomQueue<>();

        queue.enqueue("a");
        assertEquals("a", queue.peek());
        assertEquals("a", queue.peekRear());

        queue.enqueue("b");
        queue.enqueue("c");
        assertEquals("a", queue.peek());
        assertEquals("c", queue.peekRear());

        assertEquals("a", queue.dequeue());
        assertEquals("b", queue.peek());
        assertEquals("c", queue.peekRear());

        queue.enqueue("d");
        assertEquals("b", queue.peek());
        assertEquals("d", queue.peekRear());

        assertEquals("b", queue.dequeue());
        assertEquals("c", queue.dequeue());
        assertEquals("d", queue.peek());
        assertEquals("d", queue.peekRear());
    }

    @Test
    void largerSequenceOfEnqueueAndDequeueOperationsPreservesOrder() {
        CustomQueue<Integer> queue = new CustomQueue<>();

        for (int i = 1; i <= 20; i++) {
            queue.enqueue(i);
        }

        for (int i = 1; i <= 10; i++) {
            assertEquals(i, queue.dequeue());
        }

        for (int i = 21; i <= 30; i++) {
            queue.enqueue(i);
        }

        for (int i = 11; i <= 30; i++) {
            assertEquals(i, queue.dequeue());
        }

        assertTrue(queue.isEmpty());
    }
}
