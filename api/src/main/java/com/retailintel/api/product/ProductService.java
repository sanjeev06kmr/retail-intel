package com.retailintel.api.product;

import com.retailintel.api.product.dto.CreateProductRequest;
import com.retailintel.api.product.dto.UpdateProductRequest;
import com.retailintel.api.product.exception.OptimisticLockConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(CreateProductRequest request){

        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStatus(request.getStatus());

        // To be updated once we have Auth in plce
        product.setCreatedBy("system");
        product.setUpdatedBy("system");
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, UpdateProductRequest request) {
        // find existing product or throw for now
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // optimistic locking check
        if (!request.getVersion().equals(product.getVersion())) {
            throw new OptimisticLockConflictException(
                    "Optimistic locking conflict: provided version "
                            + request.getVersion()
                            + " does not match current version "
                            + product.getVersion()
            );
        }

        // update allowed fields (do not modify version, createdAt, createdBy)
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStatus(request.getStatus());

        // temporary audit change
        product.setUpdatedBy("system");

        // save and return
        return productRepository.save(product);
    }
}
