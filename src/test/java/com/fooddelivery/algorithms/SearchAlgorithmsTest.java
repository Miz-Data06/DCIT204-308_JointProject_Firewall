package com.fooddelivery.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fooddelivery.algorithms.result.BinarySearchStep;
import com.fooddelivery.algorithms.result.SearchResult;
import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;

class SearchAlgorithmsTest {
    @Test
    void linearSearchReturnsNotFoundForEmptyInput() {
        SearchResult result = new LinearSearch().search(new CustomDynamicArray<>(), "REQ-001");

        assertFalse(result.isFound());
        assertEquals(-1, result.getIndex());
        assertEquals(0, result.getOperationCount());
    }

    @Test
    void linearSearchFindsOneItemAndCountsOperation() {
        DeliveryRequest request = AlgorithmTestFixtures.request("REQ-001", 0, 60, 5);

        SearchResult result = new LinearSearch().search(AlgorithmTestFixtures.requests(request), "REQ-001");

        assertTrue(result.isFound());
        assertEquals(0, result.getIndex());
        assertSame(request, result.getRequest());
        assertEquals(1, result.getOperationCount());
        assertEquals(1, result.getLinearSearchTrace().size());
    }

    @Test
    void linearSearchFindsFirstMiddleAndLastPositions() {
        DeliveryRequest first = AlgorithmTestFixtures.request("REQ-001", 0, 60, 1);
        DeliveryRequest middle = AlgorithmTestFixtures.request("REQ-002", 1, 61, 2);
        DeliveryRequest last = AlgorithmTestFixtures.request("REQ-003", 2, 62, 3);
        CustomDynamicArray<DeliveryRequest> requests = AlgorithmTestFixtures.requests(first, middle, last);

        assertEquals(0, new LinearSearch().search(requests, "REQ-001").getIndex());
        assertEquals(1, new LinearSearch().search(requests, "REQ-002").getIndex());
        assertEquals(2, new LinearSearch().search(requests, "REQ-003").getIndex());
    }

    @Test
    void linearSearchReturnsFirstDuplicateIdWhenInputContainsDuplicates() {
        DeliveryRequest first = AlgorithmTestFixtures.request("REQ-001", 0, 60, 1);
        DeliveryRequest duplicate = AlgorithmTestFixtures.request("REQ-001", 1, 61, 2);

        SearchResult result = new LinearSearch().search(AlgorithmTestFixtures.requests(first, duplicate), "REQ-001");

        assertEquals(0, result.getIndex());
        assertSame(first, result.getRequest());
    }

    @Test
    void searchesRejectNullOrBlankInputs() {
        LinearSearch linearSearch = new LinearSearch();
        BinarySearch binarySearch = new BinarySearch();
        CustomDynamicArray<DeliveryRequest> requests = AlgorithmTestFixtures.requests(
                AlgorithmTestFixtures.request("REQ-001", 0, 60, 1));

        assertThrows(IllegalArgumentException.class, () -> linearSearch.search(null, "REQ-001"));
        assertThrows(IllegalArgumentException.class, () -> linearSearch.search(requests, null));
        assertThrows(IllegalArgumentException.class, () -> linearSearch.search(requests, " "));
        assertThrows(IllegalArgumentException.class, () -> binarySearch.search(null, "REQ-001"));
        assertThrows(IllegalArgumentException.class, () -> binarySearch.search(requests, ""));
    }

    @Test
    void binarySearchFindsItemsOnSortedInputAndRecordsTrace() {
        CustomDynamicArray<DeliveryRequest> requests = AlgorithmTestFixtures.requests(
                AlgorithmTestFixtures.request("REQ-001", 0, 60, 1),
                AlgorithmTestFixtures.request("REQ-002", 1, 61, 2),
                AlgorithmTestFixtures.request("REQ-003", 2, 62, 3));

        SearchResult result = new BinarySearch().search(requests, "REQ-002");

        assertTrue(result.isFound());
        assertEquals(1, result.getIndex());
        assertEquals(1, result.getOperationCount());
        BinarySearchStep step = result.getBinarySearchTrace().get(0);
        assertEquals(0, step.getLow());
        assertEquals(2, step.getHigh());
        assertEquals(1, step.getMiddle());
        assertEquals("REQ-002", step.getMiddleRequestId());
        assertEquals(0, step.getComparisonResult());
    }

    @Test
    void binarySearchReturnsNotFoundForMissingOneItemAndEmptyInput() {
        BinarySearch search = new BinarySearch();

        assertFalse(search.search(new CustomDynamicArray<>(), "REQ-001").isFound());
        SearchResult result = search.search(
                AlgorithmTestFixtures.requests(AlgorithmTestFixtures.request("REQ-002", 0, 60, 1)),
                "REQ-001");

        assertFalse(result.isFound());
        assertEquals(-1, result.getIndex());
        assertEquals(1, result.getOperationCount());
    }

    @Test
    void binarySearchRejectsUnsortedInputCounterexample() {
        CustomDynamicArray<DeliveryRequest> requests = AlgorithmTestFixtures.requests(
                AlgorithmTestFixtures.request("REQ-002", 0, 60, 1),
                AlgorithmTestFixtures.request("REQ-001", 1, 61, 2));

        assertThrows(IllegalArgumentException.class, () -> new BinarySearch().search(requests, "REQ-001"));
    }
}
