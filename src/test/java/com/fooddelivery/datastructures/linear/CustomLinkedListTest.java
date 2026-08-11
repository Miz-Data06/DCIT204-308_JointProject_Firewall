package com.fooddelivery.datastructures.linear;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomLinkedListTest {
    @Test
    void newListIsEmpty() {
        CustomLinkedList<String> list = new CustomLinkedList<>();

        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    void addFirstWorksOnEmptyAndNonEmptyLists() {
        CustomLinkedList<String> list = new CustomLinkedList<>();

        list.addFirst("second");
        list.addFirst("first");

        assertEquals(2, list.size());
        assertEquals("first", list.peekFirst());
        assertEquals("second", list.peekLast());
    }

    @Test
    void addLastWorksOnEmptyAndNonEmptyLists() {
        CustomLinkedList<String> list = new CustomLinkedList<>();

        list.addLast("first");
        list.addLast("second");

        assertEquals(2, list.size());
        assertEquals("first", list.peekFirst());
        assertEquals("second", list.peekLast());
    }

    @Test
    void mixedAddFirstAndAddLastOperationsKeepExpectedOrder() {
        CustomLinkedList<String> list = new CustomLinkedList<>();

        list.addLast("middle");
        list.addFirst("first");
        list.addLast("last");

        assertEquals("first", list.get(0));
        assertEquals("middle", list.get(1));
        assertEquals("last", list.get(2));
    }

    @Test
    void insertsAtBeginningMiddleAndEnd() {
        CustomLinkedList<String> list = new CustomLinkedList<>();

        list.addLast("b");
        list.addLast("d");
        list.insert(0, "a");
        list.insert(2, "c");
        list.insert(4, "e");

        assertEquals(5, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
        assertEquals("d", list.get(3));
        assertEquals("e", list.get(4));
    }

    @Test
    void getReturnsCorrectValues() {
        CustomLinkedList<Integer> list = new CustomLinkedList<>();
        list.addLast(10);
        list.addLast(20);
        list.addLast(30);

        assertEquals(10, list.get(0));
        assertEquals(20, list.get(1));
        assertEquals(30, list.get(2));
    }

    @Test
    void setReturnsPreviousValue() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("old");

        String previousValue = list.set(0, "new");

        assertEquals("old", previousValue);
        assertEquals("new", list.get(0));
    }

    @Test
    void removeFirstWorksWithOneAndMultipleNodes() {
        CustomLinkedList<String> oneNodeList = new CustomLinkedList<>();
        oneNodeList.addLast("only");

        assertEquals("only", oneNodeList.removeFirst());
        assertTrue(oneNodeList.isEmpty());

        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("first");
        list.addLast("second");
        list.addLast("third");

        assertEquals("first", list.removeFirst());
        assertEquals("second", list.peekFirst());
        assertEquals("third", list.peekLast());
        assertEquals(2, list.size());
    }

    @Test
    void removeLastWorksWithOneAndMultipleNodes() {
        CustomLinkedList<String> oneNodeList = new CustomLinkedList<>();
        oneNodeList.addLast("only");

        assertEquals("only", oneNodeList.removeLast());
        assertTrue(oneNodeList.isEmpty());

        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("first");
        list.addLast("second");
        list.addLast("third");

        assertEquals("third", list.removeLast());
        assertEquals("first", list.peekFirst());
        assertEquals("second", list.peekLast());
        assertEquals(2, list.size());
    }

    @Test
    void removesByIndexAtBeginningMiddleAndEnd() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("a");
        list.addLast("b");
        list.addLast("c");
        list.addLast("d");
        list.addLast("e");

        assertEquals("a", list.remove(0));
        assertEquals("c", list.remove(1));
        assertEquals("e", list.remove(list.size() - 1));

        assertEquals(2, list.size());
        assertEquals("b", list.get(0));
        assertEquals("d", list.get(1));
    }

    @Test
    void removingFinalNodeResetsListCorrectly() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("only");

        assertEquals("only", list.remove(0));

        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertThrows(NoSuchElementException.class, list::peekFirst);
        assertThrows(NoSuchElementException.class, list::peekLast);
    }

    @Test
    void removeValueRemovesOnlyFirstDuplicate() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("same");
        list.addLast("middle");
        list.addLast("same");

        assertTrue(list.removeValue("same"));

        assertEquals(2, list.size());
        assertEquals("middle", list.get(0));
        assertEquals("same", list.get(1));
    }

    @Test
    void nullCanBeAddedFoundAndRemovedSafely() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("before");
        list.addLast(null);
        list.addLast("after");

        assertEquals(1, list.indexOf(null));
        assertTrue(list.contains(null));
        assertTrue(list.removeValue(null));

        assertEquals(2, list.size());
        assertEquals("before", list.get(0));
        assertEquals("after", list.get(1));
        assertFalse(list.contains(null));
    }

    @Test
    void containsAndIndexOfFindPresentAndAbsentValues() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("alpha");
        list.addLast("beta");

        assertTrue(list.contains("alpha"));
        assertEquals(0, list.indexOf("alpha"));
        assertFalse(list.contains("missing"));
        assertEquals(-1, list.indexOf("missing"));
    }

    @Test
    void peekFirstAndPeekLastDoNotRemoveValues() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("first");
        list.addLast("last");

        assertEquals("first", list.peekFirst());
        assertEquals("last", list.peekLast());
        assertEquals(2, list.size());
    }

    @Test
    void emptyRemoveAndPeekMethodsThrowNoSuchElementException() {
        CustomLinkedList<String> list = new CustomLinkedList<>();

        assertThrows(NoSuchElementException.class, list::removeFirst);
        assertThrows(NoSuchElementException.class, list::removeLast);
        assertThrows(NoSuchElementException.class, list::peekFirst);
        assertThrows(NoSuchElementException.class, list::peekLast);
    }

    @Test
    void invalidIndexesThrowExpectedExceptions() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("only");

        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.set(-1, "value"));
        assertThrows(IndexOutOfBoundsException.class, () -> list.set(1, "value"));
        assertThrows(IndexOutOfBoundsException.class, () -> list.insert(-1, "value"));
        assertThrows(IndexOutOfBoundsException.class, () -> list.insert(2, "value"));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(1));
    }

    @Test
    void clearEmptiesTheList() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("first");
        list.addLast("second");

        list.clear();

        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertFalse(list.contains("first"));
        assertThrows(NoSuchElementException.class, list::peekFirst);
        assertThrows(NoSuchElementException.class, list::peekLast);
    }

    @Test
    void listCanBeReusedAfterClear() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("old");
        list.clear();

        list.addLast("new");

        assertEquals(1, list.size());
        assertEquals("new", list.peekFirst());
        assertEquals("new", list.peekLast());
    }

    @Test
    void iteratorVisitsValuesFromHeadToTail() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("a");
        list.addLast("b");
        list.addLast("c");

        CustomIterator<String> iterator = list.iterator();

        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertEquals("b", iterator.next());
        assertEquals("c", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorWorksOnEmptyList() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        CustomIterator<String> iterator = list.iterator();

        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorNextAfterExhaustionThrowsNoSuchElementException() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("only");
        CustomIterator<String> iterator = list.iterator();

        assertEquals("only", iterator.next());

        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void headAndTailRemainCorrectAfterAdditionsAndRemovals() {
        CustomLinkedList<String> list = new CustomLinkedList<>();

        list.addFirst("b");
        list.addFirst("a");
        list.addLast("c");
        list.addLast("d");
        assertEquals("a", list.peekFirst());
        assertEquals("d", list.peekLast());

        assertEquals("a", list.removeFirst());
        assertEquals("d", list.removeLast());
        assertEquals("b", list.peekFirst());
        assertEquals("c", list.peekLast());

        assertTrue(list.removeValue("c"));
        assertEquals("b", list.peekFirst());
        assertEquals("b", list.peekLast());

        assertEquals("b", list.removeLast());
        assertTrue(list.isEmpty());
        list.addLast("fresh");
        assertEquals("fresh", list.peekFirst());
        assertEquals("fresh", list.peekLast());
    }

    @Test
    void removeValueUpdatesTailWhenRemovingLastMatch() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("first");
        list.addLast("last");

        assertTrue(list.removeValue("last"));

        assertEquals("first", list.peekFirst());
        assertEquals("first", list.peekLast());
        assertEquals(1, list.size());
    }

    @Test
    void removeValueReturnsFalseWhenValueIsAbsent() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("present");

        assertFalse(list.removeValue("missing"));
        assertEquals(1, list.size());
    }
}
