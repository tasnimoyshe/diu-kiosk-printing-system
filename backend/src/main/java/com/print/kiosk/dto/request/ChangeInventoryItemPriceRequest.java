package com.print.kiosk.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ChangeInventoryItemPriceRequest {

    @NotNull(message = "New price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Item price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Item price supports at most 10 integer digits and 2 decimal places")
    private BigDecimal price;

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}