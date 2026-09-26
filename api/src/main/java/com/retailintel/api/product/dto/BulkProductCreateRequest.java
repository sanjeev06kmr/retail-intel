package com.retailintel.api.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class BulkProductCreateRequest {

    @NotEmpty
    @Size(max = 5000)
    @Valid
    private List<BulkProductCreateItem> items;

    public BulkProductCreateRequest() {
        // default constructor
    }

    public List<BulkProductCreateItem> getItems() {
        return items;
    }

    public void setItems(List<BulkProductCreateItem> items) {
        this.items = items;
    }
}
