package com.retailintel.api.batch;

import com.retailintel.api.product.dto.BulkProductCreateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping("/products")
    public ResponseEntity<BatchStatusResponse> createProductBatch(
            @Valid @RequestBody BulkProductCreateRequest request) {

        Batch batch = batchService.createBatchForBulkCreate(request);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(BatchStatusResponse.from(batch));
    }

    @PostMapping("/{batchId}/apply")
    public ResponseEntity<Void> applyBatch(
            @PathVariable Long batchId) {

        batchService.applyBatch(batchId);

        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{batchId}")
    public ResponseEntity<BatchStatusResponse> getBatchStatus(
            @PathVariable Long batchId) {

        BatchStatusResponse response =
                batchService.getBatchStatus(batchId);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
}