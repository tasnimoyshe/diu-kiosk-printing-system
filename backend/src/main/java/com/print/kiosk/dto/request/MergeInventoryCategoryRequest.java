package com.print.kiosk.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class MergeInventoryCategoryRequest {

    @NotNull(message = "Destination category is required")
    @Positive(message = "Destination category ID must be positive")
    private Long destinationCategoryId;

    public Long getDestinationCategoryId() {
        return destinationCategoryId;
    }

    public void setDestinationCategoryId(Long destinationCategoryId) {
        this.destinationCategoryId = destinationCategoryId;
    }
}