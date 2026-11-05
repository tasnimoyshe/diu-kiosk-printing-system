package com.print.kiosk.dto.response;
import com.print.kiosk.entity.PrintFileType;
import java.math.BigDecimal;
public record ProcessingFileResponse(Long id, String originalFileName, PrintFileType fileType, String mimeType, long fileSizeBytes, int pageCount, int physicalSheetsPerCopy, long totalPhysicalSheets, int copies, BigDecimal subtotal, boolean binaryAvailable) {}