package com.print.kiosk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class VerifyCashPaymentRequest {

    @NotBlank(message = "Registration number is required")
    @Size(
            max = 30,
            message = "Registration number must not exceed 30 characters"
    )
    private String registrationNumber;

    @NotBlank(message = "Cash PIN is required")
    @Pattern(
            regexp = "\\d{4}",
            message = "Cash PIN must contain exactly four digits"
    )
    private String pin;

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(
            String registrationNumber
    ) {
        this.registrationNumber = registrationNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}