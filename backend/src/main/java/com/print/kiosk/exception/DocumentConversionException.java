package com.print.kiosk.exception;

public class DocumentConversionException extends RuntimeException {
    public DocumentConversionException(String message) {
        super(message);
    }

    public DocumentConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}