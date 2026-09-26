package com.retailintel.api.product;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.retailintel.api.product.dto.CreateProductRequest;
import com.retailintel.api.product.dto.UpdateProductRequest;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        Product created = productService.createProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {

        Product updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(updated);
    }
}