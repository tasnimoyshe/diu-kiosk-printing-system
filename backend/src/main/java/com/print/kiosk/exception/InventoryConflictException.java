package com.print.kiosk.exception;

public class InventoryConflictException extends InvalidQueueActionException {

    public InventoryConflictException(String message) {
        super(message);
    }
}