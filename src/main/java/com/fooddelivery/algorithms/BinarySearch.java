package com.fooddelivery.algorithms;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.algorithms.result.BinarySearchStep;
import com.fooddelivery.algorithms.result.SearchResult;

/**
 * Binary request search over data sorted by ascending requestId.
 */
public class BinarySearch {
    /**
     * Finds a request by requestId after validating ascending requestId order.
     *
     * @param requests valid custom array sorted by ascending requestId; may be empty
     * @param requestId non-null, non-blank request ID
     * @return found/not-found result with required binary-search trace fields
     * @throws IllegalArgumentException when input is null, contains nulls, key is blank, or data is unsorted
     *
     * Preconditions: requests must be sorted by requestId. Edge cases: empty input returns not found.
     * Input mutation: none. Time: validation O(n), search loop best O(1), average/worst O(log n),
     * public method overall O(n). Space: O(log n) trace.
     * Food-delivery use: fast lookup once request records are sorted by ID.
     */
    public SearchResult search(CustomDynamicArray<DeliveryRequest> requests, String requestId) {
        RequestComparators.requireRequests(requests);
        String key = requireRequestId(requestId);
        validateSortedByRequestId(requests);

        CustomDynamicArray<BinarySearchStep> trace = new CustomDynamicArray<>();
        int low = 0;
        int high = requests.size() - 1;
        int operations = 0;

        while (low <= high) {
            int middle = low + (high - low) / 2;
            DeliveryRequest middleRequest = requests.get(middle);
            int comparison = middleRequest.getRequestId().compareTo(key);
            operations++;
            trace.add(new BinarySearchStep(low, high, middle, middleRequest.getRequestId(), comparison));

            if (comparison == 0) {
                return SearchResult.binary(true, middle, middleRequest, operations, trace);
            }
            if (comparison < 0) {
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }

        return SearchResult.binary(false, -1, null, operations, trace);
    }

    private static void validateSortedByRequestId(CustomDynamicArray<DeliveryRequest> requests) {
        for (int i = 1; i < requests.size(); i++) {
            if (requests.get(i - 1).getRequestId().compareTo(requests.get(i).getRequestId()) > 0) {
                throw new IllegalArgumentException("Binary search input must be sorted by ascending requestId");
            }
        }
    }

    private static String requireRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Request ID must not be null or blank");
        }
        return requestId;
    }
}
