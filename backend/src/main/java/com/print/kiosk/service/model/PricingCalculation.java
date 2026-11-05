package com.print.kiosk.service.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PricingCalculation(
        Long pricingConfigId,
        long pricingRevision,
        BigDecimal appliedRate,
        long totalDocumentPages,
        long totalPhysicalSheets,
        BigDecimal totalAmount,
        LocalDateTime calculatedAt,
        List<CalculatedPrintFile> files
) {
    public PricingCalculation {
        files = List.copyOf(files);
    }
}