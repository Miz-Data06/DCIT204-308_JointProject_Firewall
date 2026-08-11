package edu.ug.arcadia.datastructures.queues;

import edu.ug.arcadia.datastructures.linear.CustomIterator;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomDequeTest {
    @Test
    void newDequeIsEmptyWithSizeZero() {
        CustomDeque<String> deque = new CustomDeque<>();

        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
    }

    @Test
    void addFirstIncreasesSize() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addFirst("first");
        deque.addFirst("second");

        assertEquals(2, deque.size());
        assertFalse(deque.isEmpty());
    }

    @Test
    void addLastIncreasesSize() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addLast("first");
        deque.addLast("second");

        assertEquals(2, deque.size());
        assertFalse(deque.isEmpty());
    }

    @Test
    void addFirstPlacesValueAtFront() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addFirst("old");
        deque.addFirst("new");

        assertEquals("new", deque.peekFirst());
        assertEquals("old", deque.peekLast());
    }

    @Test
    void addLastPlacesValueAtRear() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addLast("front");
        deque.addLast("rear");

        assertEquals("front", deque.peekFirst());
        assertEquals("rear", deque.peekLast());
    }

    @Test
    void multipleAddFirstOperationsProduceCorrectOrder() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addFirst("c");
        deque.addFirst("b");
        deque.addFirst("a");

        assertEquals("a", deque.removeFirst());
        assertEquals("b", deque.removeFirst());
        assertEquals("c", deque.removeFirst());
    }

    @Test
    void multipleAddLastOperationsProduceCorrectOrder() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addLast("a");
        deque.addLast("b");
        deque.addLast("c");

        assertEquals("a", deque.removeFirst());
        assertEquals("b", deque.removeFirst());
        assertEquals("c", deque.removeFirst());
    }

    @Test
    void mixedAddFirstAndAddLastOperationsPreserveCorrectOrder() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addLast("b");
        deque.addFirst("a");
        deque.addLast("c");
        deque.addFirst("front");

        assertEquals("front", deque.removeFirst());
        assertEquals("a", deque.removeFirst());
        assertEquals("b", deque.removeFirst());
        assertEquals("c", deque.removeFirst());
    }

    @Test
    void peekFirstReturnsFrontWithoutRemovingIt() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("front");
        deque.addLast("rear");

        assertEquals("front", deque.peekFirst());
        assertEquals("front", deque.peekFirst());
        assertEquals(2, deque.size());
    }

    @Test
    void peekLastReturnsRearWithoutRemovingIt() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("front");
        deque.addLast("rear");

        assertEquals("rear", deque.peekLast());
        assertEquals("rear", deque.peekLast());
        assertEquals(2, deque.size());
    }

    @Test
    void removeFirstReturnsAndRemovesFront() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("front");
        deque.addLast("rear");

        assertEquals("front", deque.removeFirst());

        assertEquals("rear", deque.peekFirst());
    }

    @Test
    void removeLastReturnsAndRemovesRear() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("front");
        deque.addLast("rear");

        assertEquals("rear", deque.removeLast());

        assertEquals("front", deque.peekLast());
    }

    @Test
    void removeFirstDecreasesSize() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("front");
        deque.addLast("rear");

        deque.removeFirst();

        assertEquals(1, deque.size());
    }

    @Test
    void removeLastDecreasesSize() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("front");
        deque.addLast("rear");

        deque.removeLast();

        assertEquals(1, deque.size());
    }

    @Test
    void removingRepeatedlyFromFrontPreservesOrder() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("a");
        deque.addLast("b");
        deque.addLast("c");

        assertEquals("a", deque.removeFirst());
        assertEquals("b", deque.removeFirst());
        assertEquals("c", deque.removeFirst());
    }

    @Test
    void removingRepeatedlyFromRearPreservesOrder() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("a");
        deque.addLast("b");
        deque.addLast("c");

        assertEquals("c", deque.removeLast());
        assertEquals("b", deque.removeLast());
        assertEquals("a", deque.removeLast());
    }

    @Test
    void mixedRemovalsPreserveCorrectRemainingValues() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("a");
        deque.addLast("b");
        deque.addLast("c");
        deque.addLast("d");

        assertEquals("a", deque.removeFirst());
        assertEquals("d", deque.removeLast());

        assertEquals("b", deque.peekFirst());
        assertEquals("c", deque.peekLast());
        assertEquals(2, deque.size());
    }

    @Test
    void removeFirstOnEmptyDequeThrowsNoSuchElementException() {
        CustomDeque<String> deque = new CustomDeque<>();

        assertThrows(NoSuchElementException.class, deque::removeFirst);
    }

    @Test
    void removeLastOnEmptyDequeThrowsNoSuchElementException() {
        CustomDeque<String> deque = new CustomDeque<>();

        assertThrows(NoSuchElementException.class, deque::removeLast);
    }

    @Test
    void peekFirstOnEmptyDequeThrowsNoSuchElementException() {
        CustomDeque<String> deque = new CustomDeque<>();

        assertThrows(NoSuchElementException.class, deque::peekFirst);
    }

    @Test
    void peekLastOnEmptyDequeThrowsNoSuchElementException() {
        CustomDeque<String> deque = new CustomDeque<>();

        assertThrows(NoSuchElementException.class, deque::peekLast);
    }

    @Test
    void dequeAcceptsNullAtFront() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addFirst(null);

        assertEquals(null, deque.peekFirst());
        assertEquals(1, deque.size());
    }

    @Test
    void dequeAcceptsNullAtRear() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addLast(null);

        assertEquals(null, deque.peekLast());
        assertEquals(1, deque.size());
    }

    @Test
    void nullValuesCanBePeekedAndRemovedCorrectly() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addFirst(null);
        deque.addLast("tail");

        assertEquals(null, deque.peekFirst());
        assertEquals(null, deque.removeFirst());
        assertEquals("tail", deque.peekFirst());
        assertEquals("tail", deque.removeLast());
        assertTrue(deque.isEmpty());
    }

    @Test
    void duplicateValuesRemainSeparateEntries() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("same");
        deque.addLast("same");

        assertEquals(2, deque.size());
        assertEquals("same", deque.removeFirst());
        assertEquals(1, deque.size());
        assertEquals("same", deque.removeFirst());
        assertTrue(deque.isEmpty());
    }

    @Test
    void removingOnlyElementWithRemoveFirstLeavesDequeEmpty() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("only");

        assertEquals("only", deque.removeFirst());

        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
    }

    @Test
    void removingOnlyElementWithRemoveLastLeavesDequeEmpty() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("only");

        assertEquals("only", deque.removeLast());

        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
    }

    @Test
    void dequeRemainsCorrectWhenAlternatingBetweenEmptyAndNonEmptyStates() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addFirst("front");
        assertEquals("front", deque.removeLast());
        assertTrue(deque.isEmpty());

        deque.addLast("rear");
        assertEquals("rear", deque.removeFirst());
        assertTrue(deque.isEmpty());

        deque.addFirst("again");
        assertEquals("again", deque.peekFirst());
        assertEquals("again", deque.peekLast());
    }

    @Test
    void clearEmptiesDeque() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("a");
        deque.addLast("b");

        deque.clear();

        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
        assertThrows(NoSuchElementException.class, deque::peekFirst);
    }

    @Test
    void dequeCanBeReusedAfterClear() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("old");
        deque.clear();

        deque.addLast("new");

        assertEquals(1, deque.size());
        assertEquals("new", deque.peekFirst());
        assertEquals("new", deque.peekLast());
    }

    @Test
    void iteratorTraversesFromFrontToRear() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("front");
        deque.addLast("middle");
        deque.addLast("rear");

        CustomIterator<String> iterator = deque.iterator();

        assertEquals("front", iterator.next());
        assertEquals("middle", iterator.next());
        assertEquals("rear", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorReflectsMixedFrontAndRearInsertionsCorrectly() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("b");
        deque.addFirst("a");
        deque.addLast("c");

        CustomIterator<String> iterator = deque.iterator();

        assertEquals("a", iterator.next());
        assertEquals("b", iterator.next());
        assertEquals("c", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorDoesNotModifyDeque() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("front");
        deque.addLast("rear");
        CustomIterator<String> iterator = deque.iterator();

        assertEquals("front", iterator.next());
        assertEquals("rear", iterator.next());

        assertEquals(2, deque.size());
        assertEquals("front", deque.peekFirst());
        assertEquals("rear", deque.peekLast());
    }

    @Test
    void iteratorWorksOnEmptyDeque() {
        CustomDeque<String> deque = new CustomDeque<>();
        CustomIterator<String> iterator = deque.iterator();

        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorNextAfterExhaustionThrowsNoSuchElementException() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addLast("only");
        CustomIterator<String> iterator = deque.iterator();

        assertEquals("only", iterator.next());

        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void longerMixedSequencePreservesFrontRearSizeAndOrder() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addLast("c");
        deque.addFirst("b");
        deque.addLast("d");
        deque.addFirst("a");
        assertEquals(4, deque.size());
        assertEquals("a", deque.peekFirst());
        assertEquals("d", deque.peekLast());

        assertEquals("a", deque.removeFirst());
        assertEquals("d", deque.removeLast());
        deque.addLast("e");
        deque.addFirst("front");

        assertEquals(4, deque.size());
        assertEquals("front", deque.peekFirst());
        assertEquals("e", deque.peekLast());
        assertEquals("front", deque.removeFirst());
        assertEquals("b", deque.removeFirst());
        assertEquals("c", deque.removeFirst());
        assertEquals("e", deque.removeFirst());
        assertTrue(deque.isEmpty());
    }
}
