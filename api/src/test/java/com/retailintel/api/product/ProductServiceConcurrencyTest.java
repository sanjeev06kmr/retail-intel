package com.retailintel.api.product;

import com.retailintel.api.product.dto.UpdateProductRequest;
import com.retailintel.api.product.exception.OptimisticLockConflictException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import jakarta.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify optimistic locking behavior in ProductService.updateProduct.
 * Uses the real Spring context and database.
 */
@SpringBootTest
public class ProductServiceConcurrencyTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    private Long productId;

    @BeforeEach
    void setUp() {
        Product product = new Product();
        product.setSku("CONC-001");
        product.setName("Concurrent Product");
        product.setDescription("Created for concurrency test");
        product.setCategory("Test");
        product.setPrice(new BigDecimal("10.00"));
        product.setStatus("ACTIVE");
        // Ensure initial version is 0 (entity mapping/version strategy assumed)
       // product.setVersion(0L);
        product.setCreatedBy("test");
        product.setUpdatedBy("test");

        Product saved = productRepository.saveAndFlush(product);
        productId = saved.getId();
    }

    @AfterEach
    void tearDown() {
        if (productId != null && productRepository.existsById(productId)) {
            productRepository.deleteById(productId);
        }
    }

    @Test
    void concurrentUpdates_oneShouldSucceed_otherShouldFailWithOptimisticLocking() throws Exception {
        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch readsCompletedLatch = new CountDownLatch(2);

        List<Future<Boolean>> futures = new ArrayList<>();

        Callable<Boolean> task = () -> {
            // wait for signal so both start the read-phase roughly simultaneously
            startLatch.await();

            // read product from DB and capture current version
            Product dbProduct = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
            Long capturedVersion = dbProduct.getVersion();

            // build request using captured version
            UpdateProductRequest req = new UpdateProductRequest();
            req.setName("Wireless Headphones Pro");
            req.setDescription("Updated headphones");
            req.setCategory("Electronics");
            req.setPrice(new BigDecimal("119.99"));
            req.setStatus("ACTIVE");
            req.setVersion(capturedVersion);

            // signal that this task has completed the read phase
            readsCompletedLatch.countDown();

            // wait until both tasks have read the product before attempting update
            readsCompletedLatch.await();

            try {
                productService.updateProduct(productId, req);
                return true; // success
            } catch (Exception ex) {
                // propagate exception to be inspected by the caller
                throw ex;
            }
        };

        // submit two concurrent tasks
        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(task));
        }

        // release both threads to run (start read-phase)
        startLatch.countDown();

        int successCount = 0;
        int optimisticFailureCount = 0;

        for (Future<Boolean> future : futures) {
            try {
                Boolean result = future.get();
                if (Boolean.TRUE.equals(result)) {
                    successCount++;
                }
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                // Accept several possible optimistic lock related exception types
                if (cause instanceof OptimisticLockConflictException
                        || cause instanceof ObjectOptimisticLockingFailureException
                        || cause instanceof OptimisticLockException) {
                    optimisticFailureCount++;
                } else {
                    // unexpected exception -> fail the test with details
                    throw ee;
                }
            }
        }

        // exactly one should succeed, one should fail due to optimistic locking
        assertEquals(1, successCount, "Exactly one update should succeed");
        assertEquals(1, optimisticFailureCount, "Exactly one update should fail due to optimistic locking");

        executor.shutdown();
    }
}
