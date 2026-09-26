package com.retailintel.api.batch;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BatchWorker {

    private static final Logger log =
            LoggerFactory.getLogger(BatchWorker.class);

    private static final int WORKER_COUNT = 4;
    private static final int QUEUE_CAPACITY = 20;
    private static final int CHUNK_SIZE = 100;

    private final BatchProcessor batchProcessor;
    private final ThreadPoolExecutor executor;

    public BatchWorker(BatchProcessor batchProcessor) {
        this.batchProcessor = batchProcessor;

        AtomicInteger threadNumber = new AtomicInteger(1);

        this.executor = new ThreadPoolExecutor(
                WORKER_COUNT,
                WORKER_COUNT,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                runnable -> {
                    Thread thread = new Thread(
                            runnable,
                            "batch-worker-" + threadNumber.getAndIncrement()
                    );
                    thread.setDaemon(false);
                    return thread;
                },
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public void submitBatch(Long batchId) {

        log.info("Submitting batch {} to worker pool", batchId);

        for (int i = 0; i < WORKER_COUNT; i++) {

            try {
                executor.submit(() -> processBatch(batchId));
            } catch (RejectedExecutionException ex) {
                log.error(
                        "Batch {} rejected because worker queue is full",
                        batchId,
                        ex
                );

                throw ex;
            }
        }
    }

    private void processBatch(Long batchId) {

        String workerName = Thread.currentThread().getName();

        log.info(
                "Worker {} started processing batch {}",
                workerName,
                batchId
        );

        try {
            while (true) {

                boolean processed =
                        batchProcessor.processNextChunk(
                                batchId,
                                CHUNK_SIZE
                        );

                if (!processed) {
                    break;
                }
            }

            log.info(
                    "Worker {} finished processing batch {}",
                    workerName,
                    batchId
            );

        } catch (RuntimeException ex) {

            log.error(
                    "Worker {} failed processing batch {}",
                    workerName,
                    batchId,
                    ex
            );
        }
    }

    @PreDestroy
    public void shutdown() {

        log.info("Shutting down batch worker pool");

        executor.shutdown();

        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn(
                        "Batch worker pool did not terminate within 30 seconds"
                );
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();

            log.warn(
                    "Interrupted while shutting down batch worker pool",
                    ex
            );
        }
    }
}