package com.print.kiosk.converter;

public interface DocumentConverter {
    byte[] convertToPdf(String originalFileName, byte[] sourceBytes);
}