package com.print.kiosk.service.impl;

import com.print.kiosk.entity.PrintColor;
import com.print.kiosk.entity.PrintingSide;
import com.print.kiosk.service.model.AnalyzedUpload;
import com.print.kiosk.service.model.CalculatedPrintFile;
import com.print.kiosk.service.model.PricingCalculation;
import com.print.kiosk.service.model.PricingSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class PricingCalculator {

    private static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");

    public PricingCalculation calculate(
            List<AnalyzedUpload> uploads,
            PrintColor printColor,
            PrintingSide printingSide,
            int copies,
            PricingSnapshot pricingSnapshot
    ) {
        if (uploads == null || uploads.isEmpty()) {
            throw new IllegalArgumentException("At least one analyzed file is required");
        }
        if (printColor == null || printingSide == null) {
            throw new IllegalArgumentException("Print color and printing side are required");
        }
        if (copies < 1 || copies > 100) {
            throw new IllegalArgumentException("Copies must be between 1 and 100");
        }
        if (pricingSnapshot == null) {
            throw new IllegalArgumentException("Pricing snapshot is required");
        }

        BigDecimal rate = money(pricingSnapshot.priceFor(printColor, printingSide));
        LocalDateTime calculationTime = LocalDateTime.now(DHAKA_ZONE);
        List<CalculatedPrintFile> calculatedFiles = new ArrayList<>(uploads.size());
        long totalPages = 0;
        long totalSheets = 0;
        BigDecimal totalAmount = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);

        for (AnalyzedUpload upload : uploads) {
            int sheetsPerCopy = printingSide == PrintingSide.SINGLE_SIDED
                    ? upload.pageCount()
                    : (upload.pageCount() + 1) / 2;

            long fileSheets = Math.multiplyExact((long) sheetsPerCopy, copies);
            BigDecimal subtotal = money(rate.multiply(BigDecimal.valueOf(fileSheets)));

            totalPages = Math.addExact(totalPages, upload.pageCount());
            totalSheets = Math.addExact(totalSheets, fileSheets);
            totalAmount = money(totalAmount.add(subtotal));

            calculatedFiles.add(new CalculatedPrintFile(
                    upload.originalFileName(),
                    upload.fileType(),
                    upload.mimeType(),
                    upload.fileSizeBytes(),
                    upload.pageCount(),
                    sheetsPerCopy,
                    fileSheets,
                    copies,
                    rate,
                    subtotal,
                    calculationTime,
                    upload.fileData().clone()
            ));
        }

        return new PricingCalculation(
                pricingSnapshot.pricingConfigId(),
                pricingSnapshot.revision(),
                rate,
                totalPages,
                totalSheets,
                totalAmount,
                calculationTime,
                calculatedFiles
        );
    }

    private BigDecimal money(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Monetary value is required");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}