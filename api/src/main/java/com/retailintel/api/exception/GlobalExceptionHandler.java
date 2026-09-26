package com.retailintel.api.exception;

import com.retailintel.api.batch.exception.BatchNotFoundException;
import com.retailintel.api.product.exception.OptimisticLockConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OptimisticLockConflictException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockConflict(OptimisticLockConflictException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", HttpStatus.CONFLICT.getReasonPhrase());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(BatchNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBatchNotFound(
            BatchNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
