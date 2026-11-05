package com.print.kiosk.exception;

public class PricingConflictException extends RuntimeException {
    public PricingConflictException(String message) {
        super(message);
    }
}