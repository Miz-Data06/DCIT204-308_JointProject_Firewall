package com.fooddelivery.algorithms.result;

/**
 * One binary-search probe containing the required trace table fields.
 */
public final class BinarySearchStep {
    private final int low;
    private final int high;
    private final int middle;
    private final String middleRequestId;
    private final int comparisonResult;

    public BinarySearchStep(int low, int high, int middle, String middleRequestId, int comparisonResult) {
        this.low = low;
        this.high = high;
        this.middle = middle;
        this.middleRequestId = middleRequestId;
        this.comparisonResult = comparisonResult;
    }

    public int getLow() {
        return low;
    }

    public int getHigh() {
        return high;
    }

    public int getMiddle() {
        return middle;
    }

    public String getMiddleRequestId() {
        return middleRequestId;
    }

    public int getComparisonResult() {
        return comparisonResult;
    }
}
