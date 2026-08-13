package com.fooddelivery.datastructures.queues;

import com.fooddelivery.datastructures.linear.CustomIterator;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.RequestStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomDequeTest {
    private static final LocalDateTime SUBMITTED = LocalDateTime.of(2026, 8, 13, 9, 0);

    @Test
    void newDequeIsEmptyWithSizeZero() {
        CustomDeque<String> deque = new CustomDeque<>();

        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
    }

    @Test
    void addFrontInsertsIntoEmptyDeque() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addFront("front");

        assertEquals("front", deque.peekFront());
        assertEquals("front", deque.peekRear());
        assertEquals(1, deque.size());
        assertFalse(deque.isEmpty());
    }

    @Test
    void addRearInsertsIntoEmptyDeque() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addRear("rear");

        assertEquals("rear", deque.peekFront());
        assertEquals("rear", deque.peekRear());
        assertEquals(1, deque.size());
        assertFalse(deque.isEmpty());
    }

    @Test
    void addFrontIncreasesSize() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addFront("first");
        deque.addFront("second");

        assertEquals(2, deque.size());
    }

    @Test
    void addRearIncreasesSize() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addRear("first");
        deque.addRear("second");

        assertEquals(2, deque.size());
    }

    @Test
    void addFrontRejectsNull() {
        CustomDeque<String> deque = new CustomDeque<>();

        assertThrows(IllegalArgumentException.class, () -> deque.addFront(null));
    }

    @Test
    void addRearRejectsNull() {
        CustomDeque<String> deque = new CustomDeque<>();

        assertThrows(IllegalArgumentException.class, () -> deque.addRear(null));
    }

    @Test
    void peekFrontReturnsFrontWithoutRemovingOrChangingSize() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addRear("front");
        deque.addRear("rear");

        assertEquals("front", deque.peekFront());
        assertEquals("front", deque.peekFront());
        assertEquals(2, deque.size());
        assertEquals("front", deque.removeFront());
    }

    @Test
    void peekRearReturnsRearWithoutRemovingOrChangingSize() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addRear("front");
        deque.addRear("rear");

        assertEquals("rear", deque.peekRear());
        assertEquals("rear", deque.peekRear());
        assertEquals(2, deque.size());
        assertEquals("rear", deque.removeRear());
    }

    @Test
    void emptyPeekAndRemoveOperationsThrowNoSuchElementException() {
        CustomDeque<String> deque = new CustomDeque<>();

        assertThrows(NoSuchElementException.class, deque::peekFront);
        assertThrows(NoSuchElementException.class, deque::peekRear);
        assertThrows(NoSuchElementException.class, deque::removeFront);
        assertThrows(NoSuchElementException.class, deque::removeRear);
    }

    @Test
    void removeFrontReturnsOnlyElementAndLeavesDequeEmpty() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addFront("only");

        assertEquals("only", deque.removeFront());

        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
        assertThrows(NoSuchElementException.class, deque::peekFront);
        assertThrows(NoSuchElementException.class, deque::peekRear);
    }

    @Test
    void removeRearReturnsOnlyElementAndLeavesDequeEmpty() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addRear("only");

        assertEquals("only", deque.removeRear());

        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
        assertThrows(NoSuchElementException.class, deque::peekFront);
        assertThrows(NoSuchElementException.class, deque::peekRear);
    }

    @Test
    void dequeCanBeReusedAfterRemovingOnlyElementFromFront() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addRear("old");
        assertEquals("old", deque.removeFront());

        deque.addFront("new");

        assertEquals("new", deque.peekFront());
        assertEquals("new", deque.peekRear());
    }

    @Test
    void dequeCanBeReusedAfterRemovingOnlyElementFromRear() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addFront("old");
        assertEquals("old", deque.removeRear());

        deque.addRear("new");

        assertEquals("new", deque.peekFront());
        assertEquals("new", deque.peekRear());
    }

    @Test
    void removeFrontDecreasesSize() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addRear("front");
        deque.addRear("rear");

        assertEquals("front", deque.removeFront());

        assertEquals(1, deque.size());
    }

    @Test
    void removeRearDecreasesSize() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addRear("front");
        deque.addRear("rear");

        assertEquals("rear", deque.removeRear());

        assertEquals(1, deque.size());
    }

    @Test
    void frontInsertionOrderIsCorrect() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addFront("c");
        deque.addFront("b");
        deque.addFront("a");

        assertEquals("a", deque.removeFront());
        assertEquals("b", deque.removeFront());
        assertEquals("c", deque.removeFront());
    }

    @Test
    void rearInsertionOrderIsCorrect() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addRear("a");
        deque.addRear("b");
        deque.addRear("c");

        assertEquals("a", deque.removeFront());
        assertEquals("b", deque.removeFront());
        assertEquals("c", deque.removeFront());
    }

    @Test
    void mixedFrontAndRearInsertionsPreserveOrder() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addRear("b");
        deque.addFront("a");
        deque.addRear("c");
        deque.addFront("front");

        assertEquals("front", deque.removeFront());
        assertEquals("a", deque.removeFront());
        assertEquals("b", deque.removeFront());
        assertEquals("c", deque.removeFront());
    }

    @Test
    void mixedFrontAndRearRemovalsPreserveOrder() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addRear("a");
        deque.addRear("b");
        deque.addRear("c");
        deque.addRear("d");

        assertEquals("a", deque.removeFront());
        assertEquals("d", deque.removeRear());

        assertEquals("b", deque.peekFront());
        assertEquals("c", deque.peekRear());
        assertEquals(2, deque.size());
    }

    @Test
    void addingFrontAndRemovingRearWorksCorrectly() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addFront("c");
        deque.addFront("b");
        deque.addFront("a");

        assertEquals("c", deque.removeRear());
        assertEquals("b", deque.removeRear());
        assertEquals("a", deque.removeRear());
        assertTrue(deque.isEmpty());
    }

    @Test
    void addingRearAndRemovingFrontWorksCorrectly() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addRear("a");
        deque.addRear("b");
        deque.addRear("c");

        assertEquals("a", deque.removeFront());
        assertEquals("b", deque.removeFront());
        assertEquals("c", deque.removeFront());
        assertTrue(deque.isEmpty());
    }

    @Test
    void repeatedOperationsEventuallyEmptyDeque() {
        CustomDeque<Integer> deque = new CustomDeque<>();

        for (int value = 1; value <= 5; value++) {
            deque.addRear(value);
        }

        for (int expected = 1; expected <= 5; expected++) {
            assertEquals(expected, deque.removeFront());
        }

        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
    }

    @Test
    void alternatingAddRemoveOperationsPreserveConsistency() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addFront("b");
        deque.addRear("c");
        assertEquals("b", deque.removeFront());
        deque.addFront("a");
        assertEquals("c", deque.removeRear());
        deque.addRear("d");

        assertEquals("a", deque.removeFront());
        assertEquals("d", deque.removeFront());
        assertTrue(deque.isEmpty());
    }

    @Test
    void duplicateValuesAreStoredSeparatelyAndRemovedCorrectly() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addRear("same");
        deque.addRear("same");

        assertEquals(2, deque.size());
        assertEquals("same", deque.removeFront());
        assertEquals(1, deque.size());
        assertEquals("same", deque.removeFront());
        assertTrue(deque.isEmpty());
    }

    @Test
    void stringsWorkCorrectly() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addFront("middle");
        deque.addFront("front");
        deque.addRear("rear");

        assertEquals("front", deque.removeFront());
        assertEquals("rear", deque.removeRear());
        assertEquals("middle", deque.removeFront());
    }

    @Test
    void deliveryRequestObjectsWorkCorrectly() {
        CustomDeque<DeliveryRequest> deque = new CustomDeque<>();
        DeliveryRequest first = request("REQ001", 1);
        DeliveryRequest second = request("REQ002", 2);

        deque.addRear(first);
        deque.addRear(second);

        assertSame(first, deque.removeFront());
        assertSame(second, deque.removeFront());
    }

    @Test
    void urgentRequestInsertedAtFrontIsRemovedBeforeNormalRearRequests() {
        CustomDeque<DeliveryRequest> deque = new CustomDeque<>();
        DeliveryRequest normal = request("REQ001", 1);
        DeliveryRequest urgent = request("REQ002", 5);

        deque.addRear(normal);
        deque.addFront(urgent);

        assertSame(urgent, deque.removeFront());
        assertSame(normal, deque.removeFront());
    }

    @Test
    void multipleUrgentRequestsFollowFrontInsertionSemantics() {
        CustomDeque<DeliveryRequest> deque = new CustomDeque<>();
        DeliveryRequest normal = request("REQ001", 1);
        DeliveryRequest urgentOne = request("REQ002", 5);
        DeliveryRequest urgentTwo = request("REQ003", 6);

        deque.addRear(normal);
        deque.addFront(urgentOne);
        deque.addFront(urgentTwo);

        assertSame(urgentTwo, deque.removeFront());
        assertSame(urgentOne, deque.removeFront());
        assertSame(normal, deque.removeFront());
    }

    @Test
    void largerDeterministicSequencePreservesCorrectOrder() {
        CustomDeque<Integer> deque = new CustomDeque<>();

        for (int value = 10; value >= 1; value--) {
            deque.addFront(value);
        }
        for (int value = 11; value <= 20; value++) {
            deque.addRear(value);
        }

        for (int expected = 1; expected <= 20; expected++) {
            assertEquals(expected, deque.removeFront());
        }
        assertTrue(deque.isEmpty());
    }

    @Test
    void sizeRemainsAccurateThroughoutLongDeterministicSequence() {
        CustomDeque<Integer> deque = new CustomDeque<>();

        for (int value = 1; value <= 50; value++) {
            deque.addRear(value);
            assertEquals(value, deque.size());
        }
        for (int expectedSize = 49; expectedSize >= 0; expectedSize--) {
            deque.removeFront();
            assertEquals(expectedSize, deque.size());
        }
    }

    @Test
    void clearEmptiesDequeAndAllowsReuse() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addRear("a");
        deque.addRear("b");

        deque.clear();

        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
        assertThrows(NoSuchElementException.class, deque::peekFront);

        deque.addRear("new");
        assertEquals("new", deque.peekFront());
        assertEquals("new", deque.peekRear());
    }

    @Test
    void iteratorTraversesFromFrontToRear() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addRear("front");
        deque.addRear("middle");
        deque.addRear("rear");

        CustomIterator<String> iterator = deque.iterator();

        assertEquals("front", iterator.next());
        assertEquals("middle", iterator.next());
        assertEquals("rear", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorReflectsMixedFrontAndRearInsertionsCorrectly() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addRear("b");
        deque.addFront("a");
        deque.addRear("c");

        CustomIterator<String> iterator = deque.iterator();

        assertEquals("a", iterator.next());
        assertEquals("b", iterator.next());
        assertEquals("c", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorDoesNotModifyDeque() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addRear("front");
        deque.addRear("rear");
        CustomIterator<String> iterator = deque.iterator();

        assertEquals("front", iterator.next());
        assertEquals("rear", iterator.next());

        assertEquals(2, deque.size());
        assertEquals("front", deque.peekFront());
        assertEquals("rear", deque.peekRear());
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
        deque.addRear("only");
        CustomIterator<String> iterator = deque.iterator();

        assertEquals("only", iterator.next());

        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    @SuppressWarnings("deprecation")
    void addFirstBehavesIdenticallyToAddFront() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addFirst("old-front");
        deque.addFirst("new-front");

        assertEquals("new-front", deque.peekFront());
        assertEquals("old-front", deque.peekRear());
    }

    @Test
    @SuppressWarnings("deprecation")
    void addLastBehavesIdenticallyToAddRear() {
        CustomDeque<String> deque = new CustomDeque<>();

        deque.addLast("front");
        deque.addLast("rear");

        assertEquals("front", deque.peekFront());
        assertEquals("rear", deque.peekRear());
    }

    @Test
    @SuppressWarnings("deprecation")
    void removeFirstBehavesIdenticallyToRemoveFront() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addRear("front");
        deque.addRear("rear");

        assertEquals("front", deque.removeFirst());
        assertEquals("rear", deque.peekFront());
    }

    @Test
    @SuppressWarnings("deprecation")
    void removeLastBehavesIdenticallyToRemoveRear() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addRear("front");
        deque.addRear("rear");

        assertEquals("rear", deque.removeLast());
        assertEquals("front", deque.peekRear());
    }

    @Test
    @SuppressWarnings("deprecation")
    void peekFirstBehavesIdenticallyToPeekFront() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addRear("front");
        deque.addRear("rear");

        assertEquals(deque.peekFront(), deque.peekFirst());
        assertEquals(2, deque.size());
    }

    @Test
    @SuppressWarnings("deprecation")
    void peekLastBehavesIdenticallyToPeekRear() {
        CustomDeque<String> deque = new CustomDeque<>();
        deque.addRear("front");
        deque.addRear("rear");

        assertEquals(deque.peekRear(), deque.peekLast());
        assertEquals(2, deque.size());
    }

    @Test
    @SuppressWarnings("deprecation")
    void compatibilityAliasesPreserveExceptionBehaviour() {
        CustomDeque<String> deque = new CustomDeque<>();

        assertThrows(IllegalArgumentException.class, () -> deque.addFirst(null));
        assertThrows(IllegalArgumentException.class, () -> deque.addLast(null));
        assertThrows(NoSuchElementException.class, deque::removeFirst);
        assertThrows(NoSuchElementException.class, deque::removeLast);
        assertThrows(NoSuchElementException.class, deque::peekFirst);
        assertThrows(NoSuchElementException.class, deque::peekLast);
    }

    private static DeliveryRequest request(String requestId, int urgency) {
        return new DeliveryRequest(
                requestId,
                "LOC001",
                "LOC002",
                "Food Delivery",
                urgency,
                1.0,
                SUBMITTED,
                SUBMITTED.plusHours(1),
                RequestStatus.PENDING,
                0.0);
    }
}
