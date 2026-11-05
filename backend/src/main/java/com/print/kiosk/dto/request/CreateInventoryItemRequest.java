package com.print.kiosk.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateInventoryItemRequest {

    @NotBlank(message = "Item name is required")
    @Size(max = 120, message = "Item name must not exceed 120 characters")
    private String name;

    private Long categoryId;

    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String categoryName;

    @NotNull(message = "Item price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Item price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Item price supports at most 10 integer digits and 2 decimal places")
    private BigDecimal price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}