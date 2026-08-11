package com.fooddelivery.datastructures.heaps;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriorityHeapTest {
    @Test
    void defaultConstructorCreatesEmptyHeap() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        assertTrue(heap.isEmpty());
    }

    @Test
    void newHeapHasSizeZero() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        assertEquals(0, heap.size());
    }

    @Test
    void defaultCapacityIsPositive() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        assertTrue(heap.capacity() > 0);
    }

    @Test
    void customPositiveInitialCapacityIsPreserved() {
        PriorityHeap<Integer> heap = new PriorityHeap<>(3);

        assertEquals(3, heap.capacity());
    }

    @Test
    void capacityZeroThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new PriorityHeap<>(0));
    }

    @Test
    void negativeCapacityThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new PriorityHeap<>(-1));
    }

    @Test
    void addIncreasesSize() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        heap.add(10);
        heap.add(5);

        assertEquals(2, heap.size());
    }

    @Test
    void addMakesHeapNonEmpty() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        heap.add(10);

        assertFalse(heap.isEmpty());
    }

    @Test
    void addRejectsNullWithNullPointerException() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        assertThrows(NullPointerException.class, () -> heap.add(null));
    }

    @Test
    void peekReturnsOnlyElement() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();
        heap.add(7);

        assertEquals(7, heap.peek());
    }

    @Test
    void peekDoesNotRemoveElement() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();
        heap.add(7);

        assertEquals(7, heap.peek());
        assertEquals(7, heap.peek());
        assertEquals(1, heap.size());
    }

    @Test
    void peekOnEmptyHeapThrowsNoSuchElementException() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        assertThrows(NoSuchElementException.class, heap::peek);
    }

    @Test
    void removeOnEmptyHeapThrowsNoSuchElementException() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        assertThrows(NoSuchElementException.class, heap::remove);
    }

    @Test
    void removeReturnsOnlyElement() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();
        heap.add(7);

        assertEquals(7, heap.remove());
    }

    @Test
    void removingOnlyElementLeavesHeapEmpty() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();
        heap.add(7);

        heap.remove();

        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());
    }

    @Test
    void smallerValuesRiseToRoot() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        heap.add(30);
        heap.add(20);
        heap.add(10);

        assertEquals(10, heap.peek());
    }

    @Test
    void valuesAddedInAscendingOrderAreRemovedInAscendingOrder() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        heap.add(1);
        heap.add(2);
        heap.add(3);

        assertEquals(1, heap.remove());
        assertEquals(2, heap.remove());
        assertEquals(3, heap.remove());
    }

    @Test
    void valuesAddedInDescendingOrderAreRemovedInAscendingOrder() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        heap.add(5);
        heap.add(4);
        heap.add(3);
        heap.add(2);
        heap.add(1);

        assertEquals(1, heap.remove());
        assertEquals(2, heap.remove());
        assertEquals(3, heap.remove());
        assertEquals(4, heap.remove());
        assertEquals(5, heap.remove());
    }

    @Test
    void valuesAddedInMixedOrderAreRemovedInAscendingOrder() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        heap.add(8);
        heap.add(3);
        heap.add(6);
        heap.add(1);
        heap.add(5);

        assertEquals(1, heap.remove());
        assertEquals(3, heap.remove());
        assertEquals(5, heap.remove());
        assertEquals(6, heap.remove());
        assertEquals(8, heap.remove());
    }

    @Test
    void duplicateValuesAreStoredSeparately() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        heap.add(4);
        heap.add(4);

        assertEquals(2, heap.size());
    }

    @Test
    void duplicateValuesAreRemovedCorrectly() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        heap.add(4);
        heap.add(1);
        heap.add(4);

        assertEquals(1, heap.remove());
        assertEquals(4, heap.remove());
        assertEquals(4, heap.remove());
    }

    @Test
    void negativeIntegersArePrioritizedCorrectly() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        heap.add(-1);
        heap.add(-10);
        heap.add(-5);

        assertEquals(-10, heap.remove());
        assertEquals(-5, heap.remove());
        assertEquals(-1, heap.remove());
    }

    @Test
    void mixtureOfNegativeZeroAndPositiveIntegersIsHandledCorrectly() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        heap.add(0);
        heap.add(12);
        heap.add(-3);
        heap.add(7);
        heap.add(-8);

        assertEquals(-8, heap.remove());
        assertEquals(-3, heap.remove());
        assertEquals(0, heap.remove());
        assertEquals(7, heap.remove());
        assertEquals(12, heap.remove());
    }

    @Test
    void removeDecreasesSize() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();
        heap.add(2);
        heap.add(1);

        heap.remove();

        assertEquals(1, heap.size());
    }

    @Test
    void peekRemainsCorrectAfterRemove() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();
        heap.add(4);
        heap.add(1);
        heap.add(3);

        assertEquals(1, heap.remove());

        assertEquals(3, heap.peek());
    }

    @Test
    void interleavedAddAndRemoveOperationsPreserveHeapOrder() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();

        heap.add(5);
        heap.add(2);
        assertEquals(2, heap.remove());
        heap.add(1);
        heap.add(3);

        assertEquals(1, heap.remove());
        assertEquals(3, heap.remove());
        assertEquals(5, heap.remove());
    }

    @Test
    void removingRootUsesSmallerChildDuringSiftDown() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();
        heap.add(1);
        heap.add(5);
        heap.add(2);
        heap.add(6);
        heap.add(7);
        heap.add(3);

        assertEquals(1, heap.remove());

        assertEquals(2, heap.peek());
    }

    @Test
    void removingRepeatedlyEventuallyEmptiesHeap() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();
        heap.add(3);
        heap.add(1);
        heap.add(2);

        heap.remove();
        heap.remove();
        heap.remove();

        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());
    }

    @Test
    void backingArrayGrowsWhenCapacityIsReached() {
        PriorityHeap<Integer> heap = new PriorityHeap<>(2);

        heap.add(2);
        heap.add(1);
        heap.add(3);

        assertEquals(4, heap.capacity());
    }

    @Test
    void resizingPreservesEveryStoredValue() {
        PriorityHeap<Integer> heap = new PriorityHeap<>(2);

        heap.add(4);
        heap.add(1);
        heap.add(3);

        assertEquals(1, heap.remove());
        assertEquals(3, heap.remove());
        assertEquals(4, heap.remove());
    }

    @Test
    void multipleResizesPreserveHeapOrder() {
        PriorityHeap<Integer> heap = new PriorityHeap<>(1);

        for (int value = 10; value >= 1; value--) {
            heap.add(value);
        }

        for (int expected = 1; expected <= 10; expected++) {
            assertEquals(expected, heap.remove());
        }
    }

    @Test
    void heapWithInitialCapacityOneWorksCorrectly() {
        PriorityHeap<Integer> heap = new PriorityHeap<>(1);

        heap.add(2);
        heap.add(1);

        assertEquals(1, heap.remove());
        assertEquals(2, heap.remove());
    }

    @Test
    void clearEmptiesHeap() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();
        heap.add(2);
        heap.add(1);

        heap.clear();

        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());
        assertThrows(NoSuchElementException.class, heap::peek);
    }

    @Test
    void clearPreservesCurrentCapacity() {
        PriorityHeap<Integer> heap = new PriorityHeap<>(2);
        heap.add(3);
        heap.add(2);
        heap.add(1);
        int capacityBeforeClear = heap.capacity();

        heap.clear();

        assertEquals(capacityBeforeClear, heap.capacity());
    }

    @Test
    void heapCanBeReusedAfterClear() {
        PriorityHeap<Integer> heap = new PriorityHeap<>();
        heap.add(4);
        heap.clear();

        heap.add(2);
        heap.add(1);

        assertEquals(1, heap.remove());
        assertEquals(2, heap.remove());
    }

    @Test
    void stringsArePrioritizedUsingNaturalOrdering() {
        PriorityHeap<String> heap = new PriorityHeap<>();

        heap.add("delta");
        heap.add("alpha");
        heap.add("charlie");

        assertEquals("alpha", heap.remove());
        assertEquals("charlie", heap.remove());
        assertEquals("delta", heap.remove());
    }

    @Test
    void customComparableClassWorksCorrectly() {
        PriorityHeap<TaskPriority> heap = new PriorityHeap<>();

        heap.add(new TaskPriority("normal", 3));
        heap.add(new TaskPriority("urgent", 1));
        heap.add(new TaskPriority("soon", 2));

        assertEquals("urgent", heap.remove().name());
        assertEquals("soon", heap.remove().name());
        assertEquals("normal", heap.remove().name());
    }

    @Test
    void largerDeterministicSequenceIsRemovedInAscendingOrder() {
        PriorityHeap<Integer> heap = new PriorityHeap<>(3);
        int[] values = {42, 7, 19, 3, 25, 11, 0, -4, 18, 18, 100, 2, 8, 50, 1};
        int[] expected = {-4, 0, 1, 2, 3, 7, 8, 11, 18, 18, 19, 25, 42, 50, 100};

        for (int value : values) {
            heap.add(value);
        }

        for (int value : expected) {
            assertEquals(value, heap.remove());
        }

        assertTrue(heap.isEmpty());
    }

    private record TaskPriority(String name, int priority) implements Comparable<TaskPriority> {
        @Override
        public int compareTo(TaskPriority other) {
            return Integer.compare(priority, other.priority);
        }
    }
}
