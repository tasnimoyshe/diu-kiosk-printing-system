package com.print.kiosk.entity;

public enum PrintRequestStatus {
    AWAITING_PAYMENT,
    PAID,
    CLAIMED,
    DOWNLOADING,
    PRINTING,
    COMPLETED,
    CANCELLED,
    EXPIRED,
    PAYMENT_FAILED,
    FILE_ERROR,
    PAUSED
}