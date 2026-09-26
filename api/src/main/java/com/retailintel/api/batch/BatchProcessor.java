package com.retailintel.api.batch;

import com.retailintel.api.product.Product;
import com.retailintel.api.product.ProductRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
public class BatchProcessor {

    private static final String ERROR_CODE_DUPLICATE_SKU = "DUPLICATE_SKU";
    private static final String ERROR_CODE_PROCESSING_ERROR = "PROCESSING_ERROR";

    private final BatchItemRepository batchItemRepository;
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final TransactionTemplate transactionTemplate;
    private final TransactionTemplate requiresNewTransactionTemplate;

    public BatchProcessor(
        BatchItemRepository batchItemRepository,
        ProductRepository productRepository,
        BatchRepository batchRepository,
        PlatformTransactionManager transactionManager) {
    this.batchItemRepository = batchItemRepository;
    this.productRepository = productRepository;
        this.batchRepository = batchRepository;

    this.transactionTemplate = new TransactionTemplate(transactionManager);

    this.requiresNewTransactionTemplate =
        new TransactionTemplate(transactionManager);
    this.requiresNewTransactionTemplate.setPropagationBehavior(
        TransactionDefinition.PROPAGATION_REQUIRES_NEW
    );
    }

    public boolean processNextChunk(Long batchId, int chunkSize) {

    List<BatchItem> items = claimNextPendingItems(batchId, chunkSize);

        if (items.isEmpty()) {
            return false;
        }

        int processedCount = 0;
        int successCount = 0;
        int conflictCount = 0;
        int failureCount = 0;

        for (BatchItem item : items) {
            BatchItemProcessResult result = processClaimedProductCreateItem(item);

            processedCount += result.getProcessed();
            successCount += result.getSuccess();
            conflictCount += result.getConflict();
            failureCount += result.getFailure();
        }

        updateBatchCounters(
                batchId,
                processedCount,
                successCount,
                conflictCount,
                failureCount
        );

        return true;
    }

    private List<BatchItem> claimNextPendingItems(Long batchId, int limit) {
        List<BatchItem> claimed = transactionTemplate.execute(status -> {
            List<BatchItem> items =
                    batchItemRepository.findNextPendingItemsForUpdate(batchId, limit);

            if (items.isEmpty()) {
                return items;
            }

            List<Long> ids = items.stream().map(BatchItem::getId).toList();
            int updated = batchItemRepository.markItemsProcessing(ids);

            if (updated != ids.size()) {
                throw new IllegalStateException(
                        "Claimed item update mismatch for batch " + batchId
                );
            }

            for (BatchItem item : items) {
                item.setStatus("PROCESSING");
            }

            return items;
        });

        return claimed == null ? List.of() : claimed;
    }

    private BatchItemProcessResult processClaimedProductCreateItem(BatchItem item) {
        try {
            Long productId = createProductInNewTransaction(item);
            markSuccessInNewTransaction(item.getId(), productId);
            return BatchItemProcessResult.success();
        } catch (DataIntegrityViolationException ex) {
            markConflictInNewTransaction(
                    item.getId(),
                    ERROR_CODE_DUPLICATE_SKU,
                    "Duplicate SKU: '" + item.getSku() + "' already exists"
            );
            return BatchItemProcessResult.conflict();
        } catch (Exception ex) {
            markFailureInNewTransaction(
                    item.getId(),
                    ERROR_CODE_PROCESSING_ERROR,
                    "Product processing failed"
            );
            return BatchItemProcessResult.failure();
        }
    }

    private Long createProductInNewTransaction(BatchItem item) {
        Long productId = requiresNewTransactionTemplate.execute(status -> {
            Product product = new Product();
            product.setSku(item.getSku());
            product.setName(item.getName());
            product.setDescription(item.getDescription());
            product.setCategory(item.getCategory());
            product.setPrice(item.getPrice());
            product.setStatus(item.getProductStatus());
            product.setCreatedBy("system");
            product.setUpdatedBy("system");

            Product saved = productRepository.save(product);
            return saved.getId();
        });

        if (productId == null) {
            throw new IllegalStateException(
                    "Product id cannot be null for batch item " + item.getId()
            );
        }

        return productId;
    }

    private void markSuccessInNewTransaction(Long itemId, Long productId) {
        requiresNewTransactionTemplate.executeWithoutResult(status -> {
            int updated = batchItemRepository.markSuccess(itemId, productId);
            if (updated != 1) {
                throw new IllegalStateException(
                        "Expected to update 1 batch item, updated=" + updated
                );
            }
        });
    }

    private void markConflictInNewTransaction(
            Long itemId,
            String errorCode,
            String errorMessage) {

        requiresNewTransactionTemplate.executeWithoutResult(status -> {
            int updated =
                    batchItemRepository.markConflict(itemId, errorCode, errorMessage);
            if (updated != 1) {
                throw new IllegalStateException(
                        "Expected to update 1 batch item, updated=" + updated
                );
            }
        });
    }

    private void markFailureInNewTransaction(
            Long itemId,
            String errorCode,
            String errorMessage) {

        requiresNewTransactionTemplate.executeWithoutResult(status -> {
            int updated =
                    batchItemRepository.markFailure(itemId, errorCode, errorMessage);
            if (updated != 1) {
                throw new IllegalStateException(
                        "Expected to update 1 batch item, updated=" + updated
                );
            }
        });
    }

    private void updateBatchCounters(
            Long batchId,
            int processed,
            int success,
            int conflict,
            int failure) {

        batchRepository.incrementCounters(
                batchId,
                processed,
                success,
                conflict,
                failure
        );
    }
}