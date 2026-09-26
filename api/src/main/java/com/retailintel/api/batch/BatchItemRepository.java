package com.retailintel.api.batch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BatchItemRepository extends JpaRepository<BatchItem, Long> {

        List<BatchItem> findByBatchIdOrderByItemNumberAsc(Long batchId);

    @Query("SELECT COUNT(bi) FROM BatchItem bi WHERE bi.batchId = :batchId AND bi.status = 'PENDING'")
    long countPendingByBatchId(@Param("batchId") Long batchId);

    // Select and lock the next pending items for claiming.
    @Query(value = """
            SELECT *
            FROM batch_item
            WHERE batch_id = :batchId
              AND status = 'PENDING'
            ORDER BY id
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """, nativeQuery = true)
    List<BatchItem> findNextPendingItemsForUpdate(@Param("batchId") Long batchId,
                                                   @Param("limit") int limit);

    @Modifying
    @Query("UPDATE BatchItem bi SET bi.status = 'PROCESSING' WHERE bi.id IN :ids")
    int markItemsProcessing(@Param("ids") List<Long> ids);

    @Modifying
    @Query("""
            UPDATE BatchItem bi
            SET bi.status = 'SUCCESS',
                bi.productId = :productId,
                bi.errorCode = null,
                bi.errorMessage = null
            WHERE bi.id = :id
            """)
    int markSuccess(@Param("id") Long id, @Param("productId") Long productId);

    @Modifying
    @Query("""
            UPDATE BatchItem bi
            SET bi.status = 'CONFLICT',
                bi.productId = null,
                bi.errorCode = :errorCode,
                bi.errorMessage = :errorMessage
            WHERE bi.id = :id
            """)
    int markConflict(@Param("id") Long id,
                      @Param("errorCode") String errorCode,
                      @Param("errorMessage") String errorMessage);

    @Modifying
    @Query("""
            UPDATE BatchItem bi
            SET bi.status = 'FAILED',
                bi.productId = null,
                bi.errorCode = :errorCode,
                bi.errorMessage = :errorMessage
            WHERE bi.id = :id
            """)
    int markFailure(@Param("id") Long id,
                     @Param("errorCode") String errorCode,
                     @Param("errorMessage") String errorMessage);
}
