package edu.ug.arcadia.datastructures.queues;

import edu.ug.arcadia.datastructures.linear.CustomIterator;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomCircularQueueTest {
    @Test
    void newlyCreatedQueueIsEmptyWithSizeZero() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(3);

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void configuredCapacityIsReturnedCorrectly() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(7);

        assertEquals(7, queue.capacity());
    }

    @Test
    void capacityZeroThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CustomCircularQueue<>(0));
    }

    @Test
    void negativeCapacityThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CustomCircularQueue<>(-1));
    }

    @Test
    void enqueueIncreasesSize() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(3);

        queue.enqueue("first");
        queue.enqueue("second");

        assertEquals(2, queue.size());
        assertFalse(queue.isEmpty());
    }

    @Test
    void enqueuePreservesFifoOrder() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(3);

        queue.enqueue("first");
        queue.enqueue("second");
        queue.enqueue("third");

        assertEquals("first", queue.dequeue());
        assertEquals("second", queue.dequeue());
        assertEquals("third", queue.dequeue());
    }

    @Test
    void queueBecomesFullAtConfiguredCapacity() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(2);

        queue.enqueue("first");
        queue.enqueue("second");

        assertTrue(queue.isFull());
        assertEquals(2, queue.size());
    }

    @Test
    void enqueueOnFullQueueThrowsIllegalStateException() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(1);
        queue.enqueue("only");

        assertThrows(IllegalStateException.class, () -> queue.enqueue("extra"));
    }

    @Test
    void dequeueReturnsFrontValue() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(2);
        queue.enqueue("front");
        queue.enqueue("rear");

        assertEquals("front", queue.dequeue());
    }

    @Test
    void dequeueDecreasesSize() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(2);
        queue.enqueue("front");
        queue.enqueue("rear");

        queue.dequeue();

        assertEquals(1, queue.size());
        assertEquals("rear", queue.peek());
    }

    @Test
    void dequeueOnEmptyQueueThrowsNoSuchElementException() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(2);

        assertThrows(NoSuchElementException.class, queue::dequeue);
    }

    @Test
    void peekReturnsFrontWithoutRemovingIt() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(2);
        queue.enqueue("front");
        queue.enqueue("rear");

        assertEquals("front", queue.peek());
        assertEquals("front", queue.peek());
        assertEquals(2, queue.size());
    }

    @Test
    void peekRearReturnsRearWithoutRemovingIt() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(2);
        queue.enqueue("front");
        queue.enqueue("rear");

        assertEquals("rear", queue.peekRear());
        assertEquals("rear", queue.peekRear());
        assertEquals(2, queue.size());
    }

    @Test
    void peekOnEmptyQueueThrowsNoSuchElementException() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(2);

        assertThrows(NoSuchElementException.class, queue::peek);
    }

    @Test
    void peekRearOnEmptyQueueThrowsNoSuchElementException() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(2);

        assertThrows(NoSuchElementException.class, queue::peekRear);
    }

    @Test
    void oneElementQueueBecomesEmptyAfterDequeue() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(1);
        queue.enqueue("only");

        assertEquals("only", queue.dequeue());

        assertTrue(queue.isEmpty());
        assertFalse(queue.isFull());
        assertEquals(0, queue.size());
    }

    @Test
    void queueAcceptsNull() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(2);

        queue.enqueue(null);

        assertEquals(1, queue.size());
        assertFalse(queue.isEmpty());
    }

    @Test
    void nullValueCanBeEnqueuedPeekedAndDequeued() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(2);

        queue.enqueue(null);
        queue.enqueue("after");

        assertEquals(null, queue.peek());
        assertEquals(null, queue.dequeue());
        assertEquals("after", queue.peek());
    }

    @Test
    void duplicateValuesRemainSeparateEntries() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(2);

        queue.enqueue("same");
        queue.enqueue("same");

        assertEquals(2, queue.size());
        assertEquals("same", queue.dequeue());
        assertEquals(1, queue.size());
        assertEquals("same", queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    void freedPositionsAreReusedAfterDequeue() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(2);
        queue.enqueue("first");
        queue.enqueue("second");

        assertEquals("first", queue.dequeue());
        queue.enqueue("third");

        assertTrue(queue.isFull());
        assertEquals("second", queue.dequeue());
        assertEquals("third", queue.dequeue());
    }

    @Test
    void rearWrapsFromFinalArrayIndexToBeginning() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(3);
        queue.enqueue("a");
        queue.enqueue("b");
        queue.enqueue("c");
        queue.dequeue();

        queue.enqueue("d");

        assertEquals("b", queue.dequeue());
        assertEquals("c", queue.dequeue());
        assertEquals("d", queue.dequeue());
    }

    @Test
    void frontWrapsFromFinalArrayIndexToBeginning() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(3);
        queue.enqueue("a");
        queue.enqueue("b");
        queue.enqueue("c");
        queue.dequeue();
        queue.dequeue();
        queue.dequeue();
        queue.enqueue("d");

        assertEquals("d", queue.peek());
        assertEquals("d", queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    void multipleCompleteWrapAroundCyclesPreserveFifoOrder() {
        CustomCircularQueue<Integer> queue = new CustomCircularQueue<>(3);

        for (int cycle = 0; cycle < 5; cycle++) {
            queue.enqueue(cycle * 3 + 1);
            queue.enqueue(cycle * 3 + 2);
            queue.enqueue(cycle * 3 + 3);

            assertEquals(cycle * 3 + 1, queue.dequeue());
            assertEquals(cycle * 3 + 2, queue.dequeue());
            assertEquals(cycle * 3 + 3, queue.dequeue());
            assertTrue(queue.isEmpty());
        }
    }

    @Test
    void fullQueueCanBecomeNonFullAndThenFullAgain() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(2);
        queue.enqueue("a");
        queue.enqueue("b");
        assertTrue(queue.isFull());

        assertEquals("a", queue.dequeue());
        assertFalse(queue.isFull());

        queue.enqueue("c");
        assertTrue(queue.isFull());
        assertEquals("b", queue.dequeue());
        assertEquals("c", queue.dequeue());
    }

    @Test
    void clearEmptiesPartiallyFilledQueue() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(3);
        queue.enqueue("a");
        queue.enqueue("b");

        queue.clear();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertFalse(queue.isFull());
    }

    @Test
    void clearEmptiesFullWrappedQueue() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(3);
        queue.enqueue("a");
        queue.enqueue("b");
        queue.enqueue("c");
        queue.dequeue();
        queue.enqueue("d");
        assertTrue(queue.isFull());

        queue.clear();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertFalse(queue.isFull());
        assertThrows(NoSuchElementException.class, queue::peek);
    }

    @Test
    void clearPreservesCapacity() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(4);
        queue.enqueue("a");

        queue.clear();

        assertEquals(4, queue.capacity());
    }

    @Test
    void queueCanBeReusedAfterClear() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(2);
        queue.enqueue("old");
        queue.clear();

        queue.enqueue("new");

        assertEquals(1, queue.size());
        assertEquals("new", queue.peek());
        assertEquals("new", queue.peekRear());
    }

    @Test
    void iteratorTraversesNormalQueueInFifoOrder() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(3);
        queue.enqueue("front");
        queue.enqueue("middle");
        queue.enqueue("rear");

        CustomIterator<String> iterator = queue.iterator();

        assertEquals("front", iterator.next());
        assertEquals("middle", iterator.next());
        assertEquals("rear", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorTraversesWrappedQueueInFifoOrder() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(3);
        queue.enqueue("a");
        queue.enqueue("b");
        queue.enqueue("c");
        queue.dequeue();
        queue.enqueue("d");

        CustomIterator<String> iterator = queue.iterator();

        assertEquals("b", iterator.next());
        assertEquals("c", iterator.next());
        assertEquals("d", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorVisitsExactlyLogicalElementsNotUnusedPositions() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(5);
        queue.enqueue("a");
        queue.enqueue("b");
        queue.enqueue("c");
        queue.dequeue();
        queue.dequeue();

        CustomIterator<String> iterator = queue.iterator();

        assertTrue(iterator.hasNext());
        assertEquals("c", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorDoesNotModifyQueue() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(3);
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
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(2);
        CustomIterator<String> iterator = queue.iterator();

        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorNextAfterExhaustionThrowsNoSuchElementException() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(1);
        queue.enqueue("only");
        CustomIterator<String> iterator = queue.iterator();

        assertEquals("only", iterator.next());

        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void capacityOneQueueBehavesCorrectly() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(1);

        assertTrue(queue.isEmpty());
        queue.enqueue("first");
        assertTrue(queue.isFull());
        assertEquals("first", queue.peek());
        assertEquals("first", queue.peekRear());
        assertThrows(IllegalStateException.class, () -> queue.enqueue("second"));
        assertEquals("first", queue.dequeue());
        assertTrue(queue.isEmpty());
        queue.enqueue("second");
        assertEquals("second", queue.dequeue());
    }

    @Test
    void mixedSequencePreservesFrontRearSizeEmptyAndFullStates() {
        CustomCircularQueue<String> queue = new CustomCircularQueue<>(3);

        assertTrue(queue.isEmpty());
        queue.enqueue("a");
        assertEquals("a", queue.peek());
        assertEquals("a", queue.peekRear());
        assertEquals(1, queue.size());
        assertFalse(queue.isFull());

        queue.enqueue("b");
        queue.enqueue("c");
        assertTrue(queue.isFull());
        assertEquals("a", queue.peek());
        assertEquals("c", queue.peekRear());

        assertEquals("a", queue.dequeue());
        queue.enqueue("d");
        assertTrue(queue.isFull());
        assertEquals("b", queue.peek());
        assertEquals("d", queue.peekRear());

        assertEquals("b", queue.dequeue());
        assertEquals("c", queue.dequeue());
        assertEquals("d", queue.peek());
        assertEquals("d", queue.peekRear());
        assertEquals(1, queue.size());

        assertEquals("d", queue.dequeue());
        assertTrue(queue.isEmpty());
        assertFalse(queue.isFull());
    }
}
