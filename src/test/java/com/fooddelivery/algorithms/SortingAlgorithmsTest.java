package com.fooddelivery.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fooddelivery.algorithms.result.SortResult;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;

class SortingAlgorithmsTest {
    @Test
    void allSortsHandleEmptyAndOneItemInputs() {
        CustomDynamicArray<DeliveryRequest> empty = new CustomDynamicArray<>();
        DeliveryRequest only = AlgorithmTestFixtures.request("REQ-001", 0, 60, 1);
        CustomDynamicArray<DeliveryRequest> one = AlgorithmTestFixtures.requests(only);

        assertEquals("", AlgorithmTestFixtures.ids(new SelectionSort().sort(empty, RequestSortKey.REQUEST_ID).getSortedRequests()));
        assertEquals("REQ-001", AlgorithmTestFixtures.ids(new InsertionSort().sort(one, RequestSortKey.REQUEST_ID).getSortedRequests()));
        assertEquals("REQ-001", AlgorithmTestFixtures.ids(new MergeSort().sort(one, RequestSortKey.REQUEST_ID).getSortedRequests()));
        assertEquals("REQ-001", AlgorithmTestFixtures.ids(new QuickSort().sort(one, RequestSortKey.REQUEST_ID).getSortedRequests()));
    }

    @Test
    void allSortsOrderReverseSortedRequestsByRequestId() {
        CustomDynamicArray<DeliveryRequest> input = AlgorithmTestFixtures.requests(
                AlgorithmTestFixtures.request("REQ-003", 0, 60, 1),
                AlgorithmTestFixtures.request("REQ-002", 1, 61, 2),
                AlgorithmTestFixtures.request("REQ-001", 2, 62, 3));

        assertEquals("REQ-001,REQ-002,REQ-003", AlgorithmTestFixtures.ids(new SelectionSort().sort(input, RequestSortKey.REQUEST_ID).getSortedRequests()));
        assertEquals("REQ-001,REQ-002,REQ-003", AlgorithmTestFixtures.ids(new InsertionSort().sort(input, RequestSortKey.REQUEST_ID).getSortedRequests()));
        assertEquals("REQ-001,REQ-002,REQ-003", AlgorithmTestFixtures.ids(new MergeSort().sort(input, RequestSortKey.REQUEST_ID).getSortedRequests()));
        assertEquals("REQ-001,REQ-002,REQ-003", AlgorithmTestFixtures.ids(new QuickSort().sort(input, RequestSortKey.REQUEST_ID).getSortedRequests()));
    }

    @Test
    void sortsUseRequestIdTieBreakerForDuplicatePrimaryKeys() {
        DeliveryRequest b = AlgorithmTestFixtures.request("REQ-B", 0, 60, 10);
        DeliveryRequest a = AlgorithmTestFixtures.request("REQ-A", 1, 61, 10);

        SortResult result = new MergeSort().sort(AlgorithmTestFixtures.requests(b, a), RequestSortKey.PRIORITY_SCORE);

        assertEquals("REQ-A,REQ-B", AlgorithmTestFixtures.ids(result.getSortedRequests()));
    }

    @Test
    void sortsSupportEveryRequiredFieldAndDescendingPriorityDispatch() {
        DeliveryRequest first = AlgorithmTestFixtures.request("REQ-001", 20, 90, 1);
        DeliveryRequest second = AlgorithmTestFixtures.request("REQ-002", 10, 80, 3);
        DeliveryRequest third = AlgorithmTestFixtures.request("REQ-003", 30, 70, 2);
        CustomDynamicArray<DeliveryRequest> requests = AlgorithmTestFixtures.requests(first, second, third);

        assertEquals("REQ-002,REQ-001,REQ-003", AlgorithmTestFixtures.ids(new InsertionSort().sort(requests, RequestSortKey.TIME_SUBMITTED).getSortedRequests()));
        assertEquals("REQ-003,REQ-002,REQ-001", AlgorithmTestFixtures.ids(new MergeSort().sort(requests, RequestSortKey.DEADLINE).getSortedRequests()));
        assertEquals("REQ-002,REQ-003,REQ-001", AlgorithmTestFixtures.ids(new QuickSort().sort(requests, RequestSortKey.PRIORITY_SCORE, true, null).getSortedRequests()));
        assertEquals("REQ-002,REQ-003,REQ-001", AlgorithmTestFixtures.ids(new SelectionSort().sort(
                requests,
                RequestSortKey.ESTIMATED_DELIVERY_TIME,
                false,
                request -> request.getRequestId().equals("REQ-002") ? 4.0
                        : request.getRequestId().equals("REQ-003") ? 7.0 : 9.0).getSortedRequests()));
    }

    @Test
    void sortResultsPreserveObjectIdentitiesAndDoNotMutateInput() {
        DeliveryRequest first = AlgorithmTestFixtures.request("REQ-002", 0, 60, 1);
        DeliveryRequest second = AlgorithmTestFixtures.request("REQ-001", 1, 61, 2);
        CustomDynamicArray<DeliveryRequest> input = AlgorithmTestFixtures.requests(first, second);

        SortResult result = new QuickSort().sort(input, RequestSortKey.REQUEST_ID);

        assertEquals("REQ-002,REQ-001", AlgorithmTestFixtures.ids(input));
        assertFalse(result.isInputModified());
        assertSame(second, result.getSortedRequests().get(0));
        assertSame(first, result.getSortedRequests().get(1));
    }

    @Test
    void insertionAndMergeSortExposeTraceSnapshotsAndOperationCounts() {
        CustomDynamicArray<DeliveryRequest> input = AlgorithmTestFixtures.requests(
                AlgorithmTestFixtures.request("REQ-003", 0, 60, 1),
                AlgorithmTestFixtures.request("REQ-001", 1, 61, 2),
                AlgorithmTestFixtures.request("REQ-002", 2, 62, 3));

        SortResult insertion = new InsertionSort().sort(input, RequestSortKey.REQUEST_ID);
        SortResult merge = new MergeSort().sort(input, RequestSortKey.REQUEST_ID);

        assertTrue(insertion.getOperationCount() > 0);
        assertTrue(insertion.getTrace().size() > 0);
        assertTrue(merge.getOperationCount() > 0);
        assertTrue(merge.getTrace().size() > 0);
        assertEquals("REQ-001,REQ-002,REQ-003", AlgorithmTestFixtures.ids(merge.getSortedRequests()));
    }

    @Test
    void sortsRejectInvalidInputsAndEstimatedTimeProblems() {
        SelectionSort selectionSort = new SelectionSort();
        CustomDynamicArray<DeliveryRequest> requests = AlgorithmTestFixtures.requests(
                AlgorithmTestFixtures.request("REQ-001", 0, 60, 1));
        CustomDynamicArray<DeliveryRequest> withNull = new CustomDynamicArray<>();
        withNull.add(null);

        assertThrows(IllegalArgumentException.class, () -> selectionSort.sort(null, RequestSortKey.REQUEST_ID));
        assertThrows(IllegalArgumentException.class, () -> selectionSort.sort(requests, null));
        assertThrows(IllegalArgumentException.class, () -> selectionSort.sort(withNull, RequestSortKey.REQUEST_ID));
        assertThrows(IllegalArgumentException.class, () -> selectionSort.sort(requests, RequestSortKey.ESTIMATED_DELIVERY_TIME));
        assertThrows(IllegalArgumentException.class, () -> selectionSort.sort(
                requests,
                RequestSortKey.ESTIMATED_DELIVERY_TIME,
                false,
                request -> Double.NaN));
    }

    @Test
    void sortResultSnapshotsAreIndependent() {
        CustomDynamicArray<DeliveryRequest> input = AlgorithmTestFixtures.requests(
                AlgorithmTestFixtures.request("REQ-002", 0, 60, 1),
                AlgorithmTestFixtures.request("REQ-001", 1, 61, 2));

        SortResult result = new SelectionSort().sort(input, RequestSortKey.REQUEST_ID);
        CustomDynamicArray<DeliveryRequest> firstSnapshot = result.getSortedRequests();
        firstSnapshot.remove(0);

        assertEquals(2, result.getSortedRequests().size());
        assertEquals(2, result.getTrace().get(0).getRequestIdSnapshot().size());
    }
}
