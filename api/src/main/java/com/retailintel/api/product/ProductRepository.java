package com.retailintel.api.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

	long countBySku(String sku);

	Optional<Product> findBySku(String sku);
}
