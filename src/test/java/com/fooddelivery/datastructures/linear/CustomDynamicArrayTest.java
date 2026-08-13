package com.fooddelivery.datastructures.linear;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomDynamicArrayTest {
    @Test
    void newArrayIsEmptyWithSizeZero() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();

        assertTrue(array.isEmpty());
        assertEquals(0, array.size());
    }

    @Test
    void defaultCapacityIsTen() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();

        assertEquals(10, array.capacity());
    }

    @Test
    void customInitialCapacityWorks() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>(3);

        assertEquals(3, array.capacity());
        assertEquals(0, array.size());
    }

    @Test
    void invalidInitialCapacitiesThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CustomDynamicArray<>(0));
        assertThrows(IllegalArgumentException.class, () -> new CustomDynamicArray<>(-1));
    }

    @Test
    void addsAndRetrievesElements() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();

        array.add("first");
        array.add("second");

        assertEquals("first", array.get(0));
        assertEquals("second", array.get(1));
        assertEquals(2, array.size());
    }

    @Test
    void automaticallyResizesWhenCapacityIsExceeded() {
        CustomDynamicArray<Integer> array = new CustomDynamicArray<>(2);

        array.add(1);
        array.add(2);
        array.add(3);

        assertEquals(3, array.size());
        assertTrue(array.capacity() >= 3);
        assertEquals(3, array.get(2));
    }

    @Test
    void capacityDoublesDuringResizing() {
        CustomDynamicArray<Integer> array = new CustomDynamicArray<>(2);

        array.add(1);
        array.add(2);
        array.add(3);

        assertEquals(4, array.capacity());
    }

    @Test
    void insertsAtBeginningMiddleAndEnd() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();

        array.add("b");
        array.add("d");
        array.insert(0, "a");
        array.insert(2, "c");
        array.insert(4, "e");

        assertEquals(5, array.size());
        assertEquals("a", array.get(0));
        assertEquals("b", array.get(1));
        assertEquals("c", array.get(2));
        assertEquals("d", array.get(3));
        assertEquals("e", array.get(4));
    }

    @Test
    void setReturnsPreviousValue() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();
        array.add("old");

        String previousValue = array.set(0, "new");

        assertEquals("old", previousValue);
        assertEquals("new", array.get(0));
    }

    @Test
    void removesFromBeginningMiddleAndEnd() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();
        array.add("a");
        array.add("b");
        array.add("c");
        array.add("d");
        array.add("e");

        assertEquals("a", array.remove(0));
        assertEquals("c", array.remove(1));
        assertEquals("e", array.remove(array.size() - 1));

        assertEquals(2, array.size());
        assertEquals("b", array.get(0));
        assertEquals("d", array.get(1));
    }

    @Test
    void removeValueRemovesOnlyFirstDuplicate() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();
        array.add("same");
        array.add("middle");
        array.add("same");

        assertTrue(array.removeValue("same"));

        assertEquals(2, array.size());
        assertEquals("middle", array.get(0));
        assertEquals("same", array.get(1));
    }

    @Test
    void nullCanBeAddedFoundAndRemovedSafely() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();
        array.add("before");
        array.add(null);
        array.add("after");

        assertEquals(1, array.indexOf(null));
        assertTrue(array.contains(null));
        assertTrue(array.removeValue(null));

        assertEquals(2, array.size());
        assertEquals("before", array.get(0));
        assertEquals("after", array.get(1));
        assertFalse(array.contains(null));
    }

    @Test
    void containsAndIndexOfFindPresentAndAbsentValues() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();
        array.add("alpha");
        array.add("beta");

        assertTrue(array.contains("alpha"));
        assertEquals(0, array.indexOf("alpha"));
        assertFalse(array.contains("missing"));
        assertEquals(-1, array.indexOf("missing"));
    }

    @Test
    void invalidIndexesThrowIllegalArgumentException() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();
        array.add("only");

        assertThrows(IllegalArgumentException.class, () -> array.get(-1));
        assertThrows(IllegalArgumentException.class, () -> array.get(1));
        assertThrows(IllegalArgumentException.class, () -> array.get(2));
        assertThrows(IllegalArgumentException.class, () -> array.get(Integer.MIN_VALUE));
        assertThrows(IllegalArgumentException.class, () -> array.get(Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> array.set(-1, "value"));
        assertThrows(IllegalArgumentException.class, () -> array.set(1, "value"));
        assertThrows(IllegalArgumentException.class, () -> array.set(2, "value"));
        assertThrows(IllegalArgumentException.class, () -> array.insert(-1, "value"));
        assertThrows(IllegalArgumentException.class, () -> array.insert(2, "value"));
        assertThrows(IllegalArgumentException.class, () -> array.remove(-1));
        assertThrows(IllegalArgumentException.class, () -> array.remove(1));
        assertThrows(IllegalArgumentException.class, () -> array.remove(2));
    }

    @Test
    void invalidIndexExceptionMessageIncludesIndexAndSize() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();
        array.add("only");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> array.get(2));

        assertTrue(exception.getMessage().contains("2"));
        assertTrue(exception.getMessage().contains("1"));
    }

    @Test
    void failedIndexedOperationsKeepContentsSizeAndCapacityUnchanged() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>(4);
        array.add("first");
        array.add("second");
        int capacityBeforeFailure = array.capacity();

        assertThrows(IllegalArgumentException.class, () -> array.get(-1));
        assertArrayContents(array, "first", "second");
        assertEquals(capacityBeforeFailure, array.capacity());

        assertThrows(IllegalArgumentException.class, () -> array.set(2, "changed"));
        assertArrayContents(array, "first", "second");
        assertEquals(capacityBeforeFailure, array.capacity());

        assertThrows(IllegalArgumentException.class, () -> array.remove(3));
        assertArrayContents(array, "first", "second");
        assertEquals(capacityBeforeFailure, array.capacity());

        assertThrows(IllegalArgumentException.class, () -> array.insert(3, "third"));
        assertArrayContents(array, "first", "second");
        assertEquals(capacityBeforeFailure, array.capacity());
    }

    @Test
    void clearResetsLogicalContents() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>(2);
        array.add("a");
        array.add("b");
        int capacityBeforeClear = array.capacity();

        array.clear();

        assertEquals(0, array.size());
        assertTrue(array.isEmpty());
        assertEquals(capacityBeforeClear, array.capacity());
        assertEquals(-1, array.indexOf("a"));
    }

    @Test
    void arrayCanBeReusedAfterClear() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();
        array.add("old");
        array.clear();

        array.add("new");

        assertEquals(1, array.size());
        assertEquals("new", array.get(0));
    }

    @Test
    void iteratorVisitsElementsInInsertionOrder() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();
        array.add("a");
        array.add("b");
        array.add("c");

        CustomIterator<String> iterator = array.iterator();

        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertEquals("b", iterator.next());
        assertEquals("c", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorWorksOnAnEmptyArray() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();
        CustomIterator<String> iterator = array.iterator();

        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorNextAfterExhaustionThrowsNoSuchElementException() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();
        array.add("only");
        CustomIterator<String> iterator = array.iterator();

        assertEquals("only", iterator.next());

        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void removeValueReturnsFalseWhenValueIsAbsent() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();
        array.add("present");

        assertFalse(array.removeValue("missing"));
        assertEquals(1, array.size());
    }

    @Test
    void insertAcceptsIndexEqualToSize() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();
        array.add("first");

        array.insert(array.size(), "second");

        assertEquals("second", array.get(1));
    }

    @Test
    void removedLastSlotIsNotLogicallyVisibleToIterator() {
        CustomDynamicArray<String> array = new CustomDynamicArray<>();
        array.add("keep");
        array.add("remove");
        array.remove(1);

        CustomIterator<String> iterator = array.iterator();

        assertEquals("keep", iterator.next());
        assertFalse(iterator.hasNext());
    }

    private static void assertArrayContents(CustomDynamicArray<String> array, String... expectedValues) {
        assertEquals(expectedValues.length, array.size());
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(expectedValues[i], array.get(i));
        }
    }
}
