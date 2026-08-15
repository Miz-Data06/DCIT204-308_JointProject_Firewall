package com.fooddelivery.datastructures.heaps;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;
import com.fooddelivery.model.DeliveryRequest;
import com.fooddelivery.model.RequestStatus;

import java.util.NoSuchElementException;

/**
 * Binary max-heap for dispatching delivery requests by project priority rules.
 *
 * <p>The heap establishes order when a request is inserted. If a stored request's
 * priority score or status is changed in a way that affects dispatch decisions,
 * remove the request and reinsert it so the heap can restore its invariant.</p>
 */
public class PriorityHeap {
    private final CustomDynamicArray<DeliveryRequest> requests;

    /**
     * Creates an empty heap using the default CustomDynamicArray capacity.
     */
    public PriorityHeap() {
        requests = new CustomDynamicArray<>();
    }

    /**
     * Creates an empty heap using the provided CustomDynamicArray initial capacity.
     *
     * @param initialCapacity the starting capacity; must be greater than zero
     * @throws IllegalArgumentException if initialCapacity is zero or negative
     */
    public PriorityHeap(int initialCapacity) {
        requests = new CustomDynamicArray<>(initialCapacity);
    }

    /**
     * Inserts a dispatchable request in O(log n), excluding occasional dynamic-array resize.
     *
     * @param request the request to insert
     * @throws IllegalArgumentException if request is null, cancelled or delivered
     */
    public void insert(DeliveryRequest request) {
        validateInsertable(request);

        requests.add(request);
        heapifyUp(requests.size() - 1);
    }

    /**
     * Returns the next request to dispatch in O(1).
     *
     * @return the highest-priority request
     * @throws NoSuchElementException if the heap is empty
     */
    public DeliveryRequest peekMax() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty.");
        }

        return requests.get(0);
    }

    /**
     * Removes and returns the next request to dispatch in O(log n).
     *
     * @return the highest-priority request
     * @throws NoSuchElementException if the heap is empty
     */
    public DeliveryRequest extractMax() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty.");
        }

        DeliveryRequest maximumRequest = requests.get(0);
        DeliveryRequest lastRequest = requests.remove(requests.size() - 1);

        if (!isEmpty()) {
            requests.set(0, lastRequest);
            heapifyDown(0);
        }

        return maximumRequest;
    }

    /**
     * Returns true when no requests are stored in O(1).
     */
    public boolean isEmpty() {
        return requests.isEmpty();
    }

    /**
     * Returns the number of stored requests in O(1).
     */
    public int size() {
        return requests.size();
    }

    /**
     * Returns the current CustomDynamicArray capacity in O(1).
     */
    public int capacity() {
        return requests.capacity();
    }

    /**
     * Removes all heap entries in O(n) while preserving current capacity.
     */
    public void clear() {
        requests.clear();
    }

    /**
     * @deprecated use {@link #insert(DeliveryRequest)}.
     */
    @Deprecated
    public void add(DeliveryRequest request) {
        insert(request);
    }

    /**
     * @deprecated use {@link #peekMax()}.
     */
    @Deprecated
    public DeliveryRequest peek() {
        return peekMax();
    }

    /**
     * @deprecated use {@link #extractMax()}.
     */
    @Deprecated
    public DeliveryRequest remove() {
        return extractMax();
    }

    private void validateInsertable(DeliveryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null.");
        }

        RequestStatus status = request.getStatus();
        if (status == RequestStatus.CANCELLED || status == RequestStatus.DELIVERED) {
            throw new IllegalArgumentException("Cancelled and delivered requests cannot be inserted.");
        }
    }

    private void heapifyUp(int index) {
        int childIndex = index;
        while (childIndex > 0) {
            int parentIndex = (childIndex - 1) / 2;
            if (!hasHigherPriority(requests.get(childIndex), requests.get(parentIndex))) {
                return;
            }

            swap(childIndex, parentIndex);
            childIndex = parentIndex;
        }
    }

    private void heapifyDown(int index) {
        int parentIndex = index;
        while (parentIndex >= 0 && parentIndex < requests.size()) {
            int leftChildIndex = (parentIndex * 2) + 1;
            if (leftChildIndex < 0 || leftChildIndex >= requests.size()) {
                return;
            }

            int higherPriorityChildIndex = leftChildIndex;
            int rightChildIndex = leftChildIndex + 1;
            if (rightChildIndex > 0
                    && rightChildIndex < requests.size()
                    && hasHigherPriority(requests.get(rightChildIndex), requests.get(leftChildIndex))) {
                higherPriorityChildIndex = rightChildIndex;
            }

            if (!hasHigherPriority(requests.get(higherPriorityChildIndex), requests.get(parentIndex))) {
                return;
            }

            swap(parentIndex, higherPriorityChildIndex);
            parentIndex = higherPriorityChildIndex;
        }
    }

    private boolean hasHigherPriority(DeliveryRequest first, DeliveryRequest second) {
        int scoreComparison = Double.compare(first.getPriorityScore(), second.getPriorityScore());
        if (scoreComparison != 0) {
            return scoreComparison > 0;
        }

        if (first.getTimeSubmitted().isBefore(second.getTimeSubmitted())) {
            return true;
        }
        if (first.getTimeSubmitted().isAfter(second.getTimeSubmitted())) {
            return false;
        }

        return first.getRequestId().compareTo(second.getRequestId()) < 0;
    }

    private void swap(int firstIndex, int secondIndex) {
        DeliveryRequest first = requests.get(firstIndex);
        requests.set(firstIndex, requests.get(secondIndex));
        requests.set(secondIndex, first);
    }
}
