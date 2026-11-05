package com.print.kiosk.exception;

public class InvalidPrintRequestStateException extends RuntimeException {
    public InvalidPrintRequestStateException(String message) {
        super(message);
    }
}