package com.retailintel.api.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class BulkProductUpdateRequest {

    @NotEmpty
    @Valid
    private List<BulkProductUpdateItem> items;

    public BulkProductUpdateRequest() {
        // default constructor
    }

    public List<BulkProductUpdateItem> getItems() {
        return items;
    }

    public void setItems(List<BulkProductUpdateItem> items) {
        this.items = items;
    }
}
