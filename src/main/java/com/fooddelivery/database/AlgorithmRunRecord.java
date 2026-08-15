package com.fooddelivery.database;

import java.time.LocalDateTime;

public final class AlgorithmRunRecord {
    private final String runId;
    private final String algorithmName;
    private final int inputSize;
    private final int runNumber;
    private final long runtimeNs;
    private final double memoryKb;
    private final LocalDateTime executedAt;

    public AlgorithmRunRecord(
            String runId,
            String algorithmName,
            int inputSize,
            int runNumber,
            long runtimeNs,
            double memoryKb,
            LocalDateTime executedAt) {
        this.runId = requireNonBlank(runId, "Run ID");
        this.algorithmName = requireNonBlank(algorithmName, "Algorithm name");
        if (inputSize < 0) {
            throw new IllegalArgumentException("Input size must not be negative.");
        }
        if (runNumber <= 0) {
            throw new IllegalArgumentException("Run number must be positive.");
        }
        if (runtimeNs < 0L) {
            throw new IllegalArgumentException("Runtime must not be negative.");
        }
        if (!Double.isFinite(memoryKb) || memoryKb < 0.0) {
            throw new IllegalArgumentException("Memory must be finite and non-negative.");
        }
        if (executedAt == null) {
            throw new IllegalArgumentException("Executed-at timestamp cannot be null.");
        }
        this.inputSize = inputSize;
        this.runNumber = runNumber;
        this.runtimeNs = runtimeNs;
        this.memoryKb = memoryKb;
        this.executedAt = executedAt;
    }

    public String getRunId() {
        return runId;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public int getInputSize() {
        return inputSize;
    }

    public int getRunNumber() {
        return runNumber;
    }

    public long getRuntimeNs() {
        return runtimeNs;
    }

    public double getMemoryKb() {
        return memoryKb;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank.");
        }
        return value;
    }
}
