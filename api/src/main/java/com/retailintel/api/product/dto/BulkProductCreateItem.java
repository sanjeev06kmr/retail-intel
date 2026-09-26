package com.retailintel.api.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class BulkProductCreateItem {

    @NotBlank
    @Size(max = 100)
    private String sku;

    @NotBlank
    @Size(max = 255)
    private String name;

    private String description;

    @Size(max = 100)
    private String category;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal price;

    @NotBlank
    @Size(max = 30)
    private String status;

    public BulkProductCreateItem() {
        // default constructor
    }

    // getters and setters

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
