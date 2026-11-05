package com.print.kiosk.service.model;
public record FileDownloadPayload(String fileName, String mimeType, byte[] content) { public FileDownloadPayload { content = content.clone(); } }