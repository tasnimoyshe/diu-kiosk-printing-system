package com.print.kiosk.entity;

public enum RequestExceptionType {
    CORRUPTED_FILE,
    CONVERSION_FAILURE,
    MISSING_INSTRUCTION,
    PRINTER_INCOMPATIBILITY,
    UNREADABLE_IMAGE,
    PAYMENT_DISPUTE,
    MISSING_FILE,
    OTHER
}