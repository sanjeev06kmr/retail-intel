package com.retailintel.api.batch;

import java.time.LocalDateTime;

public class BatchStatusResponse {

    private Long batchId;
    private String operation;
    private String status;
    private Integer totalItems;
    private Integer processedItems;
    private Integer successCount;
    private Integer conflictCount;
    private Integer failureCount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public BatchStatusResponse() {
    }

    public static BatchStatusResponse from(Batch batch) {
        BatchStatusResponse response = new BatchStatusResponse();

        response.setBatchId(batch.getId());
        response.setOperation(batch.getOperation());
        response.setStatus(batch.getStatus());
        response.setTotalItems(batch.getTotalItems());
        response.setProcessedItems(batch.getProcessedItems());
        response.setSuccessCount(batch.getSuccessCount());
        response.setConflictCount(batch.getConflictCount());
        response.setFailureCount(batch.getFailureCount());
        response.setCreatedAt(batch.getCreatedAt());
        response.setCompletedAt(batch.getCompletedAt());

        return response;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getProcessedItems() {
        return processedItems;
    }

    public void setProcessedItems(Integer processedItems) {
        this.processedItems = processedItems;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getConflictCount() {
        return conflictCount;
    }

    public void setConflictCount(Integer conflictCount) {
        this.conflictCount = conflictCount;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}