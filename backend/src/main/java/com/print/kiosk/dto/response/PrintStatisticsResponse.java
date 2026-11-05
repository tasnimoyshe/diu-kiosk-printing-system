package com.print.kiosk.dto.response;

import java.math.BigDecimal;

public record PrintStatisticsResponse(
        long colorDocumentPages,
        long blackWhiteDocumentPages,
        long totalDocumentPages,
        long colorPhysicalSheets,
        long blackWhitePhysicalSheets,
        long totalPhysicalSheets,
        long pdfFiles,
        long documentFiles,
        long imageFiles,
        long totalFiles,
        BigDecimal colorDocumentPagePercentage,
        BigDecimal blackWhiteDocumentPagePercentage,
        BigDecimal pdfFilePercentage,
        BigDecimal documentFilePercentage,
        BigDecimal imageFilePercentage
) {
}