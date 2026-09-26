package com.retailintel.api.batch;

public class BatchItemProcessResult {

    private final int processed;
    private final int success;
    private final int conflict;
    private final int failure;

    private BatchItemProcessResult(int processed, int success, int conflict, int failure) {
        this.processed = processed;
        this.success = success;
        this.conflict = conflict;
        this.failure = failure;
    }

    public static BatchItemProcessResult success() {
        return new BatchItemProcessResult(1, 1, 0, 0);
    }

    public static BatchItemProcessResult conflict() {
        return new BatchItemProcessResult(1, 0, 1, 0);
    }

    public static BatchItemProcessResult failure() {
        return new BatchItemProcessResult(1, 0, 0, 1);
    }

    public int getProcessed() {
        return processed;
    }

    public int getSuccess() {
        return success;
    }

    public int getConflict() {
        return conflict;
    }

    public int getFailure() {
        return failure;
    }
}
