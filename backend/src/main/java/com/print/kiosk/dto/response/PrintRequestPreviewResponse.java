package com.print.kiosk.dto.response;

import com.print.kiosk.entity.PrintColor;
import com.print.kiosk.entity.PrintingSide;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PrintRequestPreviewResponse {

    private final long pricingRevision;
    private final PrintColor printColor;
    private final PrintingSide printingSide;
    private final int copies;
    private final BigDecimal appliedRate;
    private final long totalDocumentPages;
    private final long totalPhysicalSheets;
    private final BigDecimal totalAmount;
    private final LocalDateTime calculatedAt;
    private final List<PrintFileResponse> files;

    public PrintRequestPreviewResponse(
            long pricingRevision,
            PrintColor printColor,
            PrintingSide printingSide,
            int copies,
            BigDecimal appliedRate,
            long totalDocumentPages,
            long totalPhysicalSheets,
            BigDecimal totalAmount,
            LocalDateTime calculatedAt,
            List<PrintFileResponse> files
    ) {
        this.pricingRevision = pricingRevision;
        this.printColor = printColor;
        this.printingSide = printingSide;
        this.copies = copies;
        this.appliedRate = appliedRate;
        this.totalDocumentPages = totalDocumentPages;
        this.totalPhysicalSheets = totalPhysicalSheets;
        this.totalAmount = totalAmount;
        this.calculatedAt = calculatedAt;
        this.files = List.copyOf(files);
    }

    public long getPricingRevision() {
        return pricingRevision;
    }

    public PrintColor getPrintColor() {
        return printColor;
    }

    public PrintingSide getPrintingSide() {
        return printingSide;
    }

    public int getCopies() {
        return copies;
    }

    public BigDecimal getAppliedRate() {
        return appliedRate;
    }

    public long getTotalDocumentPages() {
        return totalDocumentPages;
    }

    public long getTotalPhysicalSheets() {
        return totalPhysicalSheets;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public List<PrintFileResponse> getFiles() {
        return files;
    }
}