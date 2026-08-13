package com.fooddelivery.datastructures.heaps;

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

class PriorityHeapTest {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 1, 15, 9, 0);

    @Test
    void defaultConstructorCreatesEmptyHeap() {
        PriorityHeap heap = new PriorityHeap();

        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());
    }

    @Test
    void initialCapacityConstructorCreatesEmptyHeap() {
        PriorityHeap heap = new PriorityHeap(3);

        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());
    }

    @Test
    void initialCapacityIsPreserved() {
        PriorityHeap heap = new PriorityHeap(4);

        assertEquals(4, heap.capacity());
    }

    @Test
    void initialCapacityOfOneWorks() {
        PriorityHeap heap = new PriorityHeap(1);
        DeliveryRequest request = request("SR001", 10.0, 0);

        heap.insert(request);

        assertSame(request, heap.peekMax());
    }

    @Test
    void zeroInitialCapacityIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PriorityHeap(0));
    }

    @Test
    void negativeInitialCapacityIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PriorityHeap(-1));
    }

    @Test
    void isEmptyIsTrueInitially() {
        PriorityHeap heap = new PriorityHeap();

        assertTrue(heap.isEmpty());
    }

    @Test
    void sizeIsZeroInitially() {
        PriorityHeap heap = new PriorityHeap();

        assertEquals(0, heap.size());
    }

    @Test
    void peekMaxOnEmptyThrowsNoSuchElementException() {
        PriorityHeap heap = new PriorityHeap();

        assertThrows(NoSuchElementException.class, heap::peekMax);
    }

    @Test
    void extractMaxOnEmptyThrowsNoSuchElementException() {
        PriorityHeap heap = new PriorityHeap();

        assertThrows(NoSuchElementException.class, heap::extractMax);
    }

    @SuppressWarnings("deprecation")
    @Test
    void deprecatedPeekOnEmptyHasSameExceptionBehaviour() {
        PriorityHeap heap = new PriorityHeap();

        assertThrows(NoSuchElementException.class, heap::peek);
    }

    @SuppressWarnings("deprecation")
    @Test
    void deprecatedRemoveOnEmptyHasSameExceptionBehaviour() {
        PriorityHeap heap = new PriorityHeap();

        assertThrows(NoSuchElementException.class, heap::remove);
    }

    @Test
    void insertRejectsNull() {
        PriorityHeap heap = new PriorityHeap();

        assertThrows(IllegalArgumentException.class, () -> heap.insert(null));
    }

    @SuppressWarnings("deprecation")
    @Test
    void deprecatedAddRejectsNull() {
        PriorityHeap heap = new PriorityHeap();

        assertThrows(IllegalArgumentException.class, () -> heap.add(null));
    }

    @Test
    void cancelledRequestIsRejected() {
        PriorityHeap heap = new PriorityHeap();

        assertThrows(IllegalArgumentException.class,
                () -> heap.insert(request("SR001", 10.0, 0, RequestStatus.CANCELLED)));
    }

    @Test
    void deliveredRequestIsRejected() {
        PriorityHeap heap = new PriorityHeap();

        assertThrows(IllegalArgumentException.class,
                () -> heap.insert(request("SR001", 10.0, 0, RequestStatus.DELIVERED)));
    }

    @Test
    void pendingRequestIsAccepted() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest request = request("SR001", 10.0, 0, RequestStatus.PENDING);

        heap.insert(request);

        assertSame(request, heap.peekMax());
    }

    @Test
    void assignedRequestIsAccepted() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest request = request("SR001", 10.0, 0, RequestStatus.ASSIGNED);

        heap.insert(request);

        assertSame(request, heap.peekMax());
    }

    @Test
    void pickedUpRequestIsAccepted() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest request = request("SR001", 10.0, 0, RequestStatus.PICKED_UP);

        heap.insert(request);

        assertSame(request, heap.peekMax());
    }

    @Test
    void invalidInsertionLeavesSizeUnchanged() {
        PriorityHeap heap = new PriorityHeap();

        assertThrows(IllegalArgumentException.class,
                () -> heap.insert(request("SR001", 10.0, 0, RequestStatus.CANCELLED)));

        assertEquals(0, heap.size());
    }

    @Test
    void invalidInsertionLeavesPreviousMaximumUnchanged() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest previousMaximum = request("SR001", 10.0, 0);
        heap.insert(previousMaximum);

        assertThrows(IllegalArgumentException.class,
                () -> heap.insert(request("SR002", 20.0, 1, RequestStatus.DELIVERED)));

        assertSame(previousMaximum, heap.peekMax());
        assertEquals(1, heap.size());
    }

    @Test
    void insertIncreasesSize() {
        PriorityHeap heap = new PriorityHeap();

        heap.insert(request("SR001", 10.0, 0));
        heap.insert(request("SR002", 20.0, 1));

        assertEquals(2, heap.size());
    }

    @Test
    void insertingOneRequestMakesHeapNonEmpty() {
        PriorityHeap heap = new PriorityHeap();

        heap.insert(request("SR001", 10.0, 0));

        assertFalse(heap.isEmpty());
    }

    @Test
    void peekMaxReturnsOnlyRequest() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest request = request("SR001", 10.0, 0);
        heap.insert(request);

        assertSame(request, heap.peekMax());
    }

    @Test
    void peekMaxDoesNotRemoveRequest() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest request = request("SR001", 10.0, 0);
        heap.insert(request);

        assertSame(request, heap.peekMax());
        assertSame(request, heap.peekMax());
        assertEquals(1, heap.size());
    }

    @Test
    void peekMaxDoesNotChangeSize() {
        PriorityHeap heap = new PriorityHeap();
        heap.insert(request("SR001", 10.0, 0));

        heap.peekMax();

        assertEquals(1, heap.size());
    }

    @Test
    void extractMaxReturnsOnlyRequest() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest request = request("SR001", 10.0, 0);
        heap.insert(request);

        assertSame(request, heap.extractMax());
    }

    @Test
    void extractingOnlyRequestLeavesHeapEmpty() {
        PriorityHeap heap = new PriorityHeap();
        heap.insert(request("SR001", 10.0, 0));

        heap.extractMax();

        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());
    }

    @Test
    void heapCanBeReusedAfterBecomingEmpty() {
        PriorityHeap heap = new PriorityHeap();
        heap.insert(request("SR001", 10.0, 0));
        heap.extractMax();
        DeliveryRequest next = request("SR002", 20.0, 1);

        heap.insert(next);

        assertSame(next, heap.extractMax());
    }

    @Test
    void largerPriorityScoreReachesRoot() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest lower = request("SR001", 10.0, 0);
        DeliveryRequest higher = request("SR002", 20.0, 1);

        heap.insert(lower);
        heap.insert(higher);

        assertSame(higher, heap.peekMax());
    }

    @Test
    void smallerPriorityScoreDoesNotReplaceLargerRoot() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest higher = request("SR001", 20.0, 0);
        DeliveryRequest lower = request("SR002", 10.0, 1);

        heap.insert(higher);
        heap.insert(lower);

        assertSame(higher, heap.peekMax());
    }

    @Test
    void requestsExtractFromLargestToSmallestPriorityScore() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest low = request("SR001", 1.0, 0);
        DeliveryRequest high = request("SR002", 3.0, 1);
        DeliveryRequest middle = request("SR003", 2.0, 2);

        heap.insert(low);
        heap.insert(high);
        heap.insert(middle);

        assertSame(high, heap.extractMax());
        assertSame(middle, heap.extractMax());
        assertSame(low, heap.extractMax());
    }

    @Test
    void negativeFinitePriorityScoresAreOrderedCorrectly() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest low = request("SR001", -10.0, 0);
        DeliveryRequest high = request("SR002", -1.0, 1);
        DeliveryRequest middle = request("SR003", -5.0, 2);

        heap.insert(low);
        heap.insert(high);
        heap.insert(middle);

        assertSame(high, heap.extractMax());
        assertSame(middle, heap.extractMax());
        assertSame(low, heap.extractMax());
    }

    @Test
    void zeroAndNegativeScoresAreOrderedCorrectly() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest negative = request("SR001", -1.0, 0);
        DeliveryRequest zero = request("SR002", 0.0, 1);

        heap.insert(negative);
        heap.insert(zero);

        assertSame(zero, heap.extractMax());
        assertSame(negative, heap.extractMax());
    }

    @Test
    void duplicatePriorityScoresAreStoredSeparately() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest first = request("SR001", 10.0, 0);
        DeliveryRequest second = request("SR002", 10.0, 1);

        heap.insert(first);
        heap.insert(second);

        assertEquals(2, heap.size());
        assertSame(first, heap.extractMax());
        assertSame(second, heap.extractMax());
    }

    @Test
    void equalScoresUseEarlierSubmissionTimeAtRoot() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest later = request("SR002", 10.0, 5);
        DeliveryRequest earlier = request("SR001", 10.0, 0);

        heap.insert(later);
        heap.insert(earlier);

        assertSame(earlier, heap.peekMax());
    }

    @Test
    void equalScoresExtractFromEarlierToLaterSubmissionTime() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest latest = request("SR003", 10.0, 10);
        DeliveryRequest earliest = request("SR001", 10.0, 0);
        DeliveryRequest middle = request("SR002", 10.0, 5);

        heap.insert(latest);
        heap.insert(earliest);
        heap.insert(middle);

        assertSame(earliest, heap.extractMax());
        assertSame(middle, heap.extractMax());
        assertSame(latest, heap.extractMax());
    }

    @Test
    void laterRequestWithSameScoreDoesNotReplaceEarlierRoot() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest earlier = request("SR001", 10.0, 0);
        DeliveryRequest later = request("SR002", 10.0, 1);

        heap.insert(earlier);
        heap.insert(later);

        assertSame(earlier, heap.peekMax());
    }

    @Test
    void equalScoresAndTimesUseLexicographicallySmallerRequestIdAtRoot() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest largerId = request("SR002", 10.0, 0);
        DeliveryRequest smallerId = request("SR001", 10.0, 0);

        heap.insert(largerId);
        heap.insert(smallerId);

        assertSame(smallerId, heap.peekMax());
    }

    @Test
    void equalScoresAndTimesExtractByLexicographicallyAscendingRequestId() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest third = request("SR003", 10.0, 0);
        DeliveryRequest first = request("SR001", 10.0, 0);
        DeliveryRequest second = request("SR002", 10.0, 0);

        heap.insert(third);
        heap.insert(first);
        heap.insert(second);

        assertSame(first, heap.extractMax());
        assertSame(second, heap.extractMax());
        assertSame(third, heap.extractMax());
    }

    @Test
    void requestIdsFollowStringCompareToSemantics() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest sr010 = request("SR010", 10.0, 0);
        DeliveryRequest sr002 = request("SR002", 10.0, 0);

        heap.insert(sr010);
        heap.insert(sr002);

        assertSame(sr002, heap.extractMax());
        assertSame(sr010, heap.extractMax());
    }

    @Test
    void differentObjectsWithIdenticalDispatchFieldsCanBothBeStored() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest first = request("SR001", 10.0, 0);
        DeliveryRequest second = request("SR001", 10.0, 0);

        heap.insert(first);
        heap.insert(second);

        assertEquals(2, heap.size());
    }

    @Test
    void sameObjectCanBeInsertedTwiceAndExtractedTwice() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest request = request("SR001", 10.0, 0);

        heap.insert(request);
        heap.insert(request);

        assertSame(request, heap.extractMax());
        assertSame(request, heap.extractMax());
    }

    @Test
    void extractingCompleteTiesPreservesHeapValidity() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest first = request("SR001", 10.0, 0);
        DeliveryRequest second = request("SR001", 10.0, 0);
        DeliveryRequest higher = request("SR002", 20.0, 1);

        heap.insert(first);
        heap.insert(second);
        heap.insert(higher);

        assertSame(higher, heap.extractMax());
        heap.extractMax();
        heap.extractMax();
        assertTrue(heap.isEmpty());
    }

    @Test
    void mixedInsertionOrderPreservesMaxHeapDispatchOrder() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest first = request("SR001", 40.0, 3);
        DeliveryRequest second = request("SR002", 80.0, 1);
        DeliveryRequest third = request("SR003", 80.0, 0);
        DeliveryRequest fourth = request("SR004", 15.0, 2);

        heap.insert(first);
        heap.insert(second);
        heap.insert(third);
        heap.insert(fourth);

        assertSame(third, heap.extractMax());
        assertSame(second, heap.extractMax());
        assertSame(first, heap.extractMax());
        assertSame(fourth, heap.extractMax());
    }

    @Test
    void ascendingScoresExtractInDescendingOrder() {
        PriorityHeap heap = new PriorityHeap();

        for (int score = 1; score <= 5; score++) {
            heap.insert(request("SR00" + score, score, score));
        }

        for (int expected = 5; expected >= 1; expected--) {
            assertEquals(expected, heap.extractMax().getPriorityScore());
        }
    }

    @Test
    void descendingScoresExtractInDescendingOrder() {
        PriorityHeap heap = new PriorityHeap();

        for (int score = 5; score >= 1; score--) {
            heap.insert(request("SR00" + score, score, score));
        }

        for (int expected = 5; expected >= 1; expected--) {
            assertEquals(expected, heap.extractMax().getPriorityScore());
        }
    }

    @Test
    void mixedScoresAndTieBreakersProduceExpectedDispatchOrder() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest first = request("SR001", 20.0, 3);
        DeliveryRequest second = request("SR002", 40.0, 2);
        DeliveryRequest third = request("SR003", 40.0, 1);
        DeliveryRequest fourth = request("SR004", 40.0, 1);
        DeliveryRequest fifth = request("SR000", 40.0, 1);

        heap.insert(first);
        heap.insert(second);
        heap.insert(third);
        heap.insert(fourth);
        heap.insert(fifth);

        assertSame(fifth, heap.extractMax());
        assertSame(third, heap.extractMax());
        assertSame(fourth, heap.extractMax());
        assertSame(second, heap.extractMax());
        assertSame(first, heap.extractMax());
    }

    @Test
    void extractMaxSelectsHigherPriorityChildDuringHeapifyDown() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest root = request("SR001", 100.0, 0);
        DeliveryRequest left = request("SR002", 70.0, 1);
        DeliveryRequest right = request("SR003", 90.0, 2);
        DeliveryRequest last = request("SR004", 10.0, 3);

        heap.insert(root);
        heap.insert(left);
        heap.insert(right);
        heap.insert(last);

        assertSame(root, heap.extractMax());
        assertSame(right, heap.peekMax());
    }

    @Test
    void heapifyDownHandlesRightChildWithHigherPriority() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest root = request("SR001", 100.0, 0);
        DeliveryRequest left = request("SR002", 80.0, 1);
        DeliveryRequest right = request("SR003", 90.0, 2);
        DeliveryRequest last = request("SR004", 10.0, 3);

        heap.insert(root);
        heap.insert(left);
        heap.insert(right);
        heap.insert(last);

        heap.extractMax();

        assertSame(right, heap.peekMax());
    }

    @Test
    void heapifyDownHandlesLeftChildWithHigherPriority() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest root = request("SR001", 100.0, 0);
        DeliveryRequest left = request("SR002", 90.0, 1);
        DeliveryRequest right = request("SR003", 80.0, 2);
        DeliveryRequest last = request("SR004", 10.0, 3);

        heap.insert(root);
        heap.insert(left);
        heap.insert(right);
        heap.insert(last);

        heap.extractMax();

        assertSame(left, heap.peekMax());
    }

    @Test
    void repeatedExtractionEventuallyEmptiesHeap() {
        PriorityHeap heap = new PriorityHeap();
        heap.insert(request("SR001", 30.0, 0));
        heap.insert(request("SR002", 20.0, 1));
        heap.insert(request("SR003", 10.0, 2));

        heap.extractMax();
        heap.extractMax();
        heap.extractMax();

        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());
    }

    @Test
    void interleavedInsertAndExtractOperationsPreserveOrder() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest first = request("SR001", 20.0, 0);
        DeliveryRequest second = request("SR002", 30.0, 1);
        DeliveryRequest third = request("SR003", 40.0, 2);
        DeliveryRequest fourth = request("SR004", 10.0, 3);

        heap.insert(first);
        heap.insert(second);
        assertSame(second, heap.extractMax());
        heap.insert(third);
        heap.insert(fourth);

        assertSame(third, heap.extractMax());
        assertSame(first, heap.extractMax());
        assertSame(fourth, heap.extractMax());
    }

    @Test
    void peekMaxRemainsCorrectAfterExtraction() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest first = request("SR001", 30.0, 0);
        DeliveryRequest second = request("SR002", 20.0, 1);
        DeliveryRequest third = request("SR003", 10.0, 2);

        heap.insert(first);
        heap.insert(second);
        heap.insert(third);
        heap.extractMax();

        assertSame(second, heap.peekMax());
    }

    @Test
    void extractMaxDecreasesSize() {
        PriorityHeap heap = new PriorityHeap();
        heap.insert(request("SR001", 30.0, 0));
        heap.insert(request("SR002", 20.0, 1));

        heap.extractMax();

        assertEquals(1, heap.size());
    }

    @Test
    void heapGrowsWhenInitialCapacityIsReached() {
        PriorityHeap heap = new PriorityHeap(2);

        heap.insert(request("SR001", 10.0, 0));
        heap.insert(request("SR002", 20.0, 1));
        heap.insert(request("SR003", 30.0, 2));

        assertEquals(4, heap.capacity());
    }

    @Test
    void resizingPreservesEveryRequest() {
        PriorityHeap heap = new PriorityHeap(2);
        DeliveryRequest first = request("SR001", 10.0, 0);
        DeliveryRequest second = request("SR002", 30.0, 1);
        DeliveryRequest third = request("SR003", 20.0, 2);

        heap.insert(first);
        heap.insert(second);
        heap.insert(third);

        assertSame(second, heap.extractMax());
        assertSame(third, heap.extractMax());
        assertSame(first, heap.extractMax());
    }

    @Test
    void multipleResizesPreserveDispatchOrder() {
        PriorityHeap heap = new PriorityHeap(1);

        for (int score = 1; score <= 12; score++) {
            heap.insert(request("SR" + formatThreeDigits(score), score, score));
        }

        for (int expected = 12; expected >= 1; expected--) {
            assertEquals(expected, heap.extractMax().getPriorityScore());
        }
    }

    @Test
    void capacityReflectsCustomDynamicArrayCapacity() {
        PriorityHeap heap = new PriorityHeap(2);

        assertEquals(2, heap.capacity());
        heap.insert(request("SR001", 10.0, 0));
        heap.insert(request("SR002", 20.0, 1));
        heap.insert(request("SR003", 30.0, 2));

        assertEquals(4, heap.capacity());
    }

    @Test
    void clearEmptiesHeap() {
        PriorityHeap heap = new PriorityHeap();
        heap.insert(request("SR001", 10.0, 0));
        heap.insert(request("SR002", 20.0, 1));

        heap.clear();

        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());
        assertThrows(NoSuchElementException.class, heap::peekMax);
    }

    @Test
    void clearPreservesCurrentCapacity() {
        PriorityHeap heap = new PriorityHeap(2);
        heap.insert(request("SR001", 10.0, 0));
        heap.insert(request("SR002", 20.0, 1));
        heap.insert(request("SR003", 30.0, 2));
        int capacityBeforeClear = heap.capacity();

        heap.clear();

        assertEquals(capacityBeforeClear, heap.capacity());
    }

    @Test
    void heapCanBeReusedAfterClear() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest request = request("SR001", 10.0, 0);
        heap.insert(request("SR002", 20.0, 1));
        heap.clear();

        heap.insert(request);

        assertSame(request, heap.extractMax());
    }

    @Test
    void removingRepeatedlyLeavesNoObservableStaleEntries() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest next = request("SR003", 30.0, 2);

        heap.insert(request("SR001", 10.0, 0));
        heap.insert(request("SR002", 20.0, 1));
        heap.extractMax();
        heap.extractMax();
        heap.insert(next);

        assertEquals(1, heap.size());
        assertSame(next, heap.extractMax());
        assertTrue(heap.isEmpty());
    }

    @SuppressWarnings("deprecation")
    @Test
    void deprecatedAddDelegatesToMaxHeapInsertion() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest lower = request("SR001", 10.0, 0);
        DeliveryRequest higher = request("SR002", 20.0, 1);

        heap.add(lower);
        heap.add(higher);

        assertSame(higher, heap.peekMax());
    }

    @SuppressWarnings("deprecation")
    @Test
    void deprecatedPeekReturnsSameRequestAsPeekMax() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest request = request("SR001", 10.0, 0);
        heap.insert(request);

        assertSame(heap.peekMax(), heap.peek());
    }

    @SuppressWarnings("deprecation")
    @Test
    void deprecatedRemoveReturnsSameRequestAsExtractMax() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest request = request("SR001", 10.0, 0);
        heap.insert(request);

        assertSame(request, heap.remove());
    }

    @SuppressWarnings("deprecation")
    @Test
    void compatibilityAliasesPreserveMaxHeapOrdering() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest lower = request("SR001", 10.0, 0);
        DeliveryRequest higher = request("SR002", 20.0, 1);

        heap.add(lower);
        heap.add(higher);

        assertSame(higher, heap.remove());
        assertSame(lower, heap.remove());
    }

    @SuppressWarnings("deprecation")
    @Test
    void compatibilityAliasesRejectTerminalStatusesThroughInsertValidation() {
        PriorityHeap heap = new PriorityHeap();

        assertThrows(IllegalArgumentException.class,
                () -> heap.add(request("SR001", 10.0, 0, RequestStatus.CANCELLED)));
        assertThrows(IllegalArgumentException.class,
                () -> heap.add(request("SR002", 10.0, 0, RequestStatus.DELIVERED)));
    }

    @Test
    void priorityAffectingMutationRequiresRemovalAndReinsertion() {
        PriorityHeap heap = new PriorityHeap();
        DeliveryRequest originalMaximum = request("SR001", 20.0, 0);
        DeliveryRequest changedAfterRemoval = request("SR002", 10.0, 1);
        DeliveryRequest comparisonRequest = request("SR003", 25.0, 2);

        heap.insert(originalMaximum);
        heap.insert(changedAfterRemoval);
        assertSame(originalMaximum, heap.extractMax());
        DeliveryRequest removed = heap.extractMax();
        removed.setPriorityScore(30.0);
        heap.insert(comparisonRequest);
        heap.insert(removed);

        assertSame(changedAfterRemoval, heap.peekMax());
    }

    @Test
    void largerDeterministicSequenceExtractsInDispatchOrder() {
        PriorityHeap heap = new PriorityHeap(3);
        DeliveryRequest[] requests = {
                request("SR009", 9.0, 9),
                request("SR001", 12.0, 4),
                request("SR004", 12.0, 4),
                request("SR003", 12.0, 1),
                request("SR010", -1.0, 0),
                request("SR002", 0.0, 7),
                request("SR006", 30.0, 6),
                request("SR005", 30.0, 3),
                request("SR007", 30.0, 3),
                request("SR008", 9.0, 1),
                request("SR011", 5.0, 2),
                request("SR012", 5.0, 8)
        };

        for (DeliveryRequest request : requests) {
            heap.insert(request);
        }

        int extractedCount = 0;
        DeliveryRequest previous = heap.extractMax();
        extractedCount++;
        while (!heap.isEmpty()) {
            DeliveryRequest current = heap.extractMax();
            extractedCount++;
            assertTrue(isDispatchBeforeOrEqual(previous, current));
            previous = current;
        }

        assertEquals(requests.length, extractedCount);
        assertEquals(0, heap.size());
    }

    private static DeliveryRequest request(String requestId, double priorityScore, int submittedMinutes) {
        return request(requestId, priorityScore, submittedMinutes, RequestStatus.PENDING);
    }

    private static DeliveryRequest request(
            String requestId,
            double priorityScore,
            int submittedMinutes,
            RequestStatus status) {
        LocalDateTime submitted = BASE_TIME.plusMinutes(submittedMinutes);
        return new DeliveryRequest(
                requestId,
                "LOC001",
                "LOC002",
                "Food",
                1,
                1.0,
                submitted,
                submitted.plusHours(1),
                status,
                priorityScore);
    }

    private static boolean isDispatchBeforeOrEqual(DeliveryRequest first, DeliveryRequest second) {
        int scoreComparison = Double.compare(first.getPriorityScore(), second.getPriorityScore());
        if (scoreComparison != 0) {
            return scoreComparison > 0;
        }

        if (!first.getTimeSubmitted().equals(second.getTimeSubmitted())) {
            return first.getTimeSubmitted().isBefore(second.getTimeSubmitted());
        }

        return first.getRequestId().compareTo(second.getRequestId()) <= 0;
    }

    private static String formatThreeDigits(int value) {
        if (value >= 100) {
            return Integer.toString(value);
        }
        if (value >= 10) {
            return "0" + value;
        }
        return "00" + value;
    }
}
