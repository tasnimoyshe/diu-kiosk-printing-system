package com.print.kiosk.service.model;

import com.print.kiosk.entity.PrintFileType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CalculatedPrintFile(
        String originalFileName,
        PrintFileType fileType,
        String mimeType,
        long fileSizeBytes,
        int pageCount,
        int physicalSheetsPerCopy,
        long totalPhysicalSheets,
        int copies,
        BigDecimal appliedRate,
        BigDecimal subtotal,
        LocalDateTime calculatedAt,
        byte[] fileData
) {
}