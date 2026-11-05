package com.print.kiosk.repository;

import com.print.kiosk.entity.PrintFileType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PrintRequestFileMetadataProjection {
    Long getId();
    String getOriginalFileName();
    PrintFileType getFileType();
    String getMimeType();
    long getFileSizeBytes();
    int getPageCount();
    int getPhysicalSheetsPerCopy();
    long getTotalPhysicalSheets();
    int getCopies();
    BigDecimal getAppliedRate();
    BigDecimal getSubtotal();
    LocalDateTime getCalculatedAt();
    LocalDateTime getUploadedAt();
}