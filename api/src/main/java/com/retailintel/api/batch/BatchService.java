package com.retailintel.api.batch;

import com.retailintel.api.batch.BatchStatusResponse;
import com.retailintel.api.product.dto.BulkProductCreateItem;
import com.retailintel.api.product.dto.BulkProductCreateRequest;
import com.retailintel.api.batch.exception.BatchNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class BatchService {

    private final BatchRepository batchRepository;
    private final BatchItemRepository batchItemRepository;
    private final BatchWorker batchWorker;

    public BatchService(BatchRepository batchRepository,
                        BatchItemRepository batchItemRepository,
                        BatchWorker batchWorker) {
        this.batchRepository = batchRepository;
        this.batchItemRepository = batchItemRepository;
        this.batchWorker = batchWorker;
    }

    // create Batch and BatchItems transactionally
    @Transactional
    public Batch createBatchForBulkCreate(BulkProductCreateRequest request) {
        int totalItems = request.getItems() == null ? 0 : request.getItems().size();

        Batch batch = new Batch();
        batch.setOperation("PRODUCT_CREATE");
        batch.setStatus("QUEUED");
        batch.setTotalItems(totalItems);
        batch.setProcessedItems(0);
        batch.setSuccessCount(0);
        batch.setConflictCount(0);
        batch.setFailureCount(0);
        batch.setCreatedBy("system");
        batch.setCreatedAt(LocalDateTime.now());

        Batch saved = batchRepository.save(batch);

        List<BatchItem> items = new ArrayList<>(totalItems);
        int itemNumber = 1;
        if (request.getItems() != null) {
            for (BulkProductCreateItem reqItem : request.getItems()) {
                BatchItem bi = new BatchItem();
                bi.setBatchId(saved.getId());
                bi.setItemNumber(itemNumber++);
                bi.setOperation("PRODUCT_CREATE");
                bi.setStatus("PENDING");
                bi.setSku(reqItem.getSku());
                bi.setName(reqItem.getName());
                bi.setDescription(reqItem.getDescription());
                bi.setCategory(reqItem.getCategory());
                bi.setPrice(reqItem.getPrice());
                bi.setProductStatus(reqItem.getStatus());
                bi.setExpectedVersion(null);
                items.add(bi);
            }
        }

        batchItemRepository.saveAll(items);
        return saved;
    }

    @Transactional
    public void applyBatch(Long batchId) {
        // Attempt atomic QUEUED -> PROCESSING transition
        int updated = batchRepository.markProcessingIfQueued(batchId);

        if (updated == 0) {
            // If no row was updated, determine whether the batch exists to choose the correct exception
            Batch existing = batchRepository.findById(batchId)
                    .orElseThrow(() -> new BatchNotFoundException("Batch not found with id: " + batchId));

            // Batch exists but was not QUEUED
            throw new IllegalStateException(
                    "Batch " + batchId + " cannot be applied because its current status is " + existing.getStatus()
            );
        }

        // At this point the status was updated in the DB. Register after-commit submission.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    batchWorker.submitBatch(batchId);
                }
            });
        } else {
            throw new IllegalStateException("No active transaction while applying batch " + batchId);
        }
    }

    public Batch getBatch(Long id) {
        return batchRepository.findById(id).orElse(null);
    }

    public BatchStatusResponse getBatchStatus(Long id) {
        return batchRepository.findById(id)
                .map(BatchStatusResponse::from)
                .orElse(null);
    }
}
