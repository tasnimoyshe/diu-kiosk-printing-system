package com.print.kiosk.dto.response;

import com.print.kiosk.entity.PrintFileType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PrintFileResponse {

    private final Long id;
    private final String originalFileName;
    private final PrintFileType fileType;
    private final String mimeType;
    private final long fileSizeBytes;
    private final int pageCount;
    private final int physicalSheetsPerCopy;
    private final long totalPhysicalSheets;
    private final int copies;
    private final BigDecimal appliedRate;
    private final BigDecimal subtotal;
    private final LocalDateTime calculatedAt;
    private final LocalDateTime uploadedAt;

    public PrintFileResponse(
            Long id,
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
            LocalDateTime uploadedAt
    ) {
        this.id = id;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.mimeType = mimeType;
        this.fileSizeBytes = fileSizeBytes;
        this.pageCount = pageCount;
        this.physicalSheetsPerCopy = physicalSheetsPerCopy;
        this.totalPhysicalSheets = totalPhysicalSheets;
        this.copies = copies;
        this.appliedRate = appliedRate;
        this.subtotal = subtotal;
        this.calculatedAt = calculatedAt;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public PrintFileType getFileType() {
        return fileType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getPhysicalSheetsPerCopy() {
        return physicalSheetsPerCopy;
    }

    public long getTotalPhysicalSheets() {
        return totalPhysicalSheets;
    }

    public int getCopies() {
        return copies;
    }

    public BigDecimal getAppliedRate() {
        return appliedRate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}