package com.print.kiosk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CancelPrintRequestRequest {

    @NotBlank(message = "ID or registration number is required for verification")
    @Pattern(
            regexp = "^\\d{3}-\\d{2}-\\d{1,6}$",
            message = "ID or registration number must follow the format 242-15-125"
    )
    private String registrationNumber;

    @Size(max = 200, message = "Cancellation reason must not exceed 200 characters")
    private String reason;

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}