package com.retailintel.api.product.exception;

public class OptimisticLockConflictException extends RuntimeException {

    public OptimisticLockConflictException(String message) {
        super(message);
    }
}
