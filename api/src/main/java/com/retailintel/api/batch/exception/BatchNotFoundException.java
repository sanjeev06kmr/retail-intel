package com.retailintel.api.batch.exception;

public class BatchNotFoundException extends RuntimeException {

    public BatchNotFoundException(String message) {
        super(message);
    }
}