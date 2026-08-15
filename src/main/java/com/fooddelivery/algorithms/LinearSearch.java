package com.fooddelivery.algorithms;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.algorithms.result.SearchResult;

/**
 * Linear request search over unsorted delivery request data.
 */
public class LinearSearch {
    /**
     * Finds the first request whose requestId equals the supplied key.
     *
     * @param requests valid custom array of delivery requests; may be empty
     * @param requestId non-null, non-blank request ID
     * @return found/not-found result with index, request, operation count and trace
     * @throws IllegalArgumentException for null input, null entries, or blank keys
     *
     * Preconditions: no ordering is required. Edge cases: empty input returns not found.
     * Input mutation: none. Time: best O(1), average O(n), worst O(n). Space: O(n) trace.
     * Food-delivery use: locate a request in an arrival-order or unsorted work list.
     */
    public SearchResult search(CustomDynamicArray<DeliveryRequest> requests, String requestId) {
        RequestComparators.requireRequests(requests);
        String key = requireRequestId(requestId);
        CustomDynamicArray<String> trace = new CustomDynamicArray<>();
        int operations = 0;

        for (int i = 0; i < requests.size(); i++) {
            DeliveryRequest request = requests.get(i);
            operations++;
            trace.add("index=" + i + ", requestId=" + request.getRequestId());
            if (request.getRequestId().equals(key)) {
                return SearchResult.linear(true, i, request, operations, trace);
            }
        }

        return SearchResult.linear(false, -1, null, operations, trace);
    }

    private static String requireRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Request ID must not be null or blank");
        }
        return requestId;
    }
}
