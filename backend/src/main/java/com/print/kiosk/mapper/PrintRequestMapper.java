package com.print.kiosk.mapper;
import com.print.kiosk.dto.request.CreatePrintRequestRequest;
import com.print.kiosk.dto.response.*;
import com.print.kiosk.entity.*;
import com.print.kiosk.repository.PrintRequestFileMetadataProjection;
import com.print.kiosk.service.model.*;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class PrintRequestMapper{
    public PrintRequest toEntity(CreatePrintRequestRequest request,PricingCalculation c){PrintRequest r=new PrintRequest();r.setFullName(request.getFullName().trim());r.setRegistrationNumber(request.getRegistrationNumber().trim());r.setUserType(request.getUserType());r.setDepartment(normalizeDepartment(request.getDepartment()));r.setPrintColor(request.getPrintColor());r.setPrintingSide(request.getPrintingSide());r.setCopies(request.getCopies());r.setPricingConfigId(c.pricingConfigId());r.setPricingRevision(c.pricingRevision());r.setAppliedRate(c.appliedRate());r.setTotalDocumentPages(c.totalDocumentPages());r.setTotalPhysicalSheets(c.totalPhysicalSheets());r.setTotalAmount(c.totalAmount());r.setCalculatedAt(c.calculatedAt());r.setPaymentStatus(PaymentStatus.PENDING);return r;}
    public PrintRequestPreviewResponse toPreviewResponse(CreatePrintRequestRequest r,PricingCalculation c){return new PrintRequestPreviewResponse(c.pricingRevision(),r.getPrintColor(),r.getPrintingSide(),r.getCopies(),c.appliedRate(),c.totalDocumentPages(),c.totalPhysicalSheets(),c.totalAmount(),c.calculatedAt(),c.files().stream().map(this::toPreviewFileResponse).toList());}
    public PrintRequestResponse toResponse(PrintRequest r,List<PrintFileResponse> files){return new PrintRequestResponse(r.getId(),r.getTrackingNumber(),r.getFullName(),r.getRegistrationNumber(),r.getUserType(),r.getDepartment(),r.getPrintColor(),r.getPrintingSide(),r.getCopies(),r.getStatus(),r.getPaymentStatus(),r.getPricingConfigId(),r.getPricingRevision(),r.getAppliedRate(),r.getTotalDocumentPages(),r.getTotalPhysicalSheets(),r.getTotalAmount(),r.getCalculatedAt(),r.getExpiresAt(),r.getCancelledAt(),r.getCancellationReason(),r.getCreatedAt(),files);}
    public PrintFileResponse toFileResponse(PrintRequestFileMetadataProjection f){return new PrintFileResponse(f.getId(),f.getOriginalFileName(),f.getFileType(),f.getMimeType(),f.getFileSizeBytes(),f.getPageCount(),f.getPhysicalSheetsPerCopy(),f.getTotalPhysicalSheets(),f.getCopies(),f.getAppliedRate(),f.getSubtotal(),f.getCalculatedAt(),f.getUploadedAt());}
    public PrintFileResponse toSavedFileResponse(PrintRequestFile f){return new PrintFileResponse(f.getId(),f.getOriginalFileName(),f.getFileType(),f.getMimeType(),f.getFileSizeBytes(),f.getPageCount(),f.getPhysicalSheetsPerCopy(),f.getTotalPhysicalSheets(),f.getCopies(),f.getAppliedRate(),f.getSubtotal(),f.getCalculatedAt(),f.getUploadedAt());}
    private PrintFileResponse toPreviewFileResponse(CalculatedPrintFile f){return new PrintFileResponse(null,f.originalFileName(),f.fileType(),f.mimeType(),f.fileSizeBytes(),f.pageCount(),f.physicalSheetsPerCopy(),f.totalPhysicalSheets(),f.copies(),f.appliedRate(),f.subtotal(),f.calculatedAt(),null);}
    private String normalizeDepartment(String d){return d==null||d.isBlank()?null:d.trim();}
}