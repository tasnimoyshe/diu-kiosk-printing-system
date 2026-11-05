package com.print.kiosk.exception;

public class AccountNotAllowedException extends RuntimeException {

    public AccountNotAllowedException(String message) {
        super(message);
    }
}