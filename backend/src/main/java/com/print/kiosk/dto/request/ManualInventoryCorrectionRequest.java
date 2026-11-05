package com.print.kiosk.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ManualInventoryCorrectionRequest {

    @NotNull(message = "Correction quantity is required")
    @Min(value = -1, message = "Correction quantity must be -1 or 1")
    @Max(value = 1, message = "Correction quantity must be -1 or 1")
    private Integer quantity;

    @NotBlank(message = "Correction reason is required")
    @Size(max = 300, message = "Reason must not exceed 300 characters")
    private String reason;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}