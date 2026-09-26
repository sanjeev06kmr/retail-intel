package com.retailintel.api.batch;

import com.retailintel.api.product.Product;
import com.retailintel.api.product.ProductRepository;
import com.retailintel.api.product.dto.BulkProductCreateItem;
import com.retailintel.api.product.dto.BulkProductCreateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class BatchConflictHandlingIntegrationTest {

    @Autowired
    private BatchService batchService;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private BatchItemRepository batchItemRepository;

    @Autowired
    private ProductRepository productRepository;

    private Long batchId;
    private String skuA;
    private String skuB;

    @AfterEach
    void tearDown() {
        if (batchId != null) {
            List<BatchItem> items = batchItemRepository.findByBatchIdOrderByItemNumberAsc(batchId);
            if (!items.isEmpty()) {
                batchItemRepository.deleteAll(items);
            }
            batchRepository.deleteById(batchId);
        }

        deleteProductBySkuIfExists(skuA);
        deleteProductBySkuIfExists(skuB);
    }

    @Test
    void duplicateSku_shouldMarkOnlyConflictingItemAndKeepPriorSuccessesCommitted() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        skuA = "CONFLICT-A-" + suffix;
        skuB = "CONFLICT-B-" + suffix;

        BulkProductCreateRequest request = new BulkProductCreateRequest();
        request.setItems(List.of(
                createItem(skuA, "Item A1"),
                createItem(skuB, "Item B"),
                createItem(skuA, "Item A2 Duplicate")
        ));

        Batch batch = batchService.createBatchForBulkCreate(request);
        batchId = batch.getId();

        batchService.applyBatch(batchId);

        Batch completedBatch = waitForBatchCompletion(batchId, 10_000);

        assertEquals("COMPLETED", completedBatch.getStatus());
        assertEquals(3, completedBatch.getTotalItems());
        assertEquals(3, completedBatch.getProcessedItems());
        assertEquals(2, completedBatch.getSuccessCount());
        assertEquals(1, completedBatch.getConflictCount());
        assertEquals(0, completedBatch.getFailureCount());

        assertEquals(1L, productRepository.countBySku(skuA));
        assertEquals(1L, productRepository.countBySku(skuB));

        List<BatchItem> items = batchItemRepository.findByBatchIdOrderByItemNumberAsc(batchId);

        assertEquals(3, items.size());

        BatchItem item1 = items.get(0);
        BatchItem item2 = items.get(1);
        BatchItem item3 = items.get(2);

        assertEquals("SUCCESS", item1.getStatus());
        assertEquals("SUCCESS", item2.getStatus());
        assertEquals("CONFLICT", item3.getStatus());

        assertNotNull(item1.getProductId());
        assertNotNull(item2.getProductId());
        assertNull(item3.getProductId());

        assertEquals("DUPLICATE_SKU", item3.getErrorCode());
        assertTrue(item3.getErrorMessage() != null && item3.getErrorMessage().contains("Duplicate SKU"));
    }

    private Batch waitForBatchCompletion(Long id, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            Batch batch = batchRepository.findById(id)
                    .orElseThrow(() -> new IllegalStateException("Batch not found: " + id));

            if ("COMPLETED".equals(batch.getStatus())) {
                return batch;
            }

            Thread.sleep(100);
        }

        Batch lastBatch = batchRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Batch not found: " + id));

        throw new AssertionError(
                "Timed out waiting for batch completion. Last status=" + lastBatch.getStatus()
                        + ", processedItems=" + lastBatch.getProcessedItems()
                        + ", successCount=" + lastBatch.getSuccessCount()
                        + ", conflictCount=" + lastBatch.getConflictCount()
                        + ", failureCount=" + lastBatch.getFailureCount()
        );
    }

    private BulkProductCreateItem createItem(String sku, String name) {
        BulkProductCreateItem item = new BulkProductCreateItem();
        item.setSku(sku);
        item.setName(name);
        item.setDescription("Batch test item");
        item.setCategory("Test");
        item.setPrice(new BigDecimal("9.99"));
        item.setStatus("ACTIVE");
        return item;
    }

    private void deleteProductBySkuIfExists(String sku) {
        if (sku == null) {
            return;
        }

        Optional<Product> product = productRepository.findBySku(sku);
        product.ifPresent(value -> productRepository.deleteById(value.getId()));
    }
}
