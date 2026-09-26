package com.retailintel.api.batch;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    @Modifying
    @Transactional
    @Query("""
    UPDATE Batch b
    SET b.processedItems = b.processedItems + :processed,
        b.successCount = b.successCount + :success,
        b.conflictCount = b.conflictCount + :conflict,
        b.failureCount = b.failureCount + :failure,
        b.status = CASE
            WHEN b.processedItems + :processed = b.totalItems
            THEN 'COMPLETED'
            ELSE b.status
        END,
        b.completedAt = CASE
            WHEN b.processedItems + :processed = b.totalItems
            THEN CURRENT_TIMESTAMP
            ELSE b.completedAt
        END
    WHERE b.id = :id
      AND b.status = 'PROCESSING'
    """)
    int incrementCounters(
            @Param("id") Long id,
            @Param("processed") int processed,
            @Param("success") int success,
            @Param("conflict") int conflict,
            @Param("failure") int failure);

    @Modifying
    @Transactional
    @Query("UPDATE Batch b SET b.status = :status WHERE b.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE Batch b SET b.status = :status, b.completedAt = :completedAt WHERE b.id = :id")
    int updateStatusAndCompletedAt(@Param("id") Long id, @Param("status") String status, @Param("completedAt") LocalDateTime completedAt);

    @Modifying
    @Query(value = """
            UPDATE batch
            SET status = 'PROCESSING'
            WHERE id = :batchId
              AND status = 'QUEUED'
            """, nativeQuery = true)
    int markProcessingIfQueued(@Param("batchId") Long batchId);
}
