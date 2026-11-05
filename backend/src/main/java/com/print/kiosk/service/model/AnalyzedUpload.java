package com.print.kiosk.service.model;

import com.print.kiosk.entity.PrintFileType;

public record AnalyzedUpload(
        String originalFileName,
        PrintFileType fileType,
        String mimeType,
        long fileSizeBytes,
        int pageCount,
        byte[] fileData
) {
}