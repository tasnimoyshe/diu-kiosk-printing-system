package com.print.kiosk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InventoryReasonRequest {

    @NotBlank(message = "Reason is required")
    @Size(max = 300, message = "Reason must not exceed 300 characters")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
