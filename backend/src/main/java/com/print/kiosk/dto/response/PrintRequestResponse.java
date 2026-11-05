package com.print.kiosk.dto.response;

import com.print.kiosk.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PrintRequestResponse {
    private final Long id; private final String trackingNumber; private final String fullName; private final String registrationNumber;
    private final UserType userType; private final String department; private final PrintColor printColor; private final PrintingSide printingSide;
    private final int copies; private final PrintRequestStatus status; private final PaymentStatus paymentStatus; private final Long pricingConfigId;
    private final long pricingRevision; private final BigDecimal appliedRate; private final long totalDocumentPages; private final long totalPhysicalSheets;
    private final BigDecimal totalAmount; private final LocalDateTime calculatedAt; private final LocalDateTime expiresAt; private final LocalDateTime cancelledAt;
    private final String cancellationReason; private final LocalDateTime createdAt; private final List<PrintFileResponse> files;
    public PrintRequestResponse(Long id,String trackingNumber,String fullName,String registrationNumber,UserType userType,String department,PrintColor printColor,PrintingSide printingSide,int copies,PrintRequestStatus status,PaymentStatus paymentStatus,Long pricingConfigId,long pricingRevision,BigDecimal appliedRate,long totalDocumentPages,long totalPhysicalSheets,BigDecimal totalAmount,LocalDateTime calculatedAt,LocalDateTime expiresAt,LocalDateTime cancelledAt,String cancellationReason,LocalDateTime createdAt,List<PrintFileResponse> files){
        this.id=id;this.trackingNumber=trackingNumber;this.fullName=fullName;this.registrationNumber=registrationNumber;this.userType=userType;this.department=department;this.printColor=printColor;this.printingSide=printingSide;this.copies=copies;this.status=status;this.paymentStatus=paymentStatus;this.pricingConfigId=pricingConfigId;this.pricingRevision=pricingRevision;this.appliedRate=appliedRate;this.totalDocumentPages=totalDocumentPages;this.totalPhysicalSheets=totalPhysicalSheets;this.totalAmount=totalAmount;this.calculatedAt=calculatedAt;this.expiresAt=expiresAt;this.cancelledAt=cancelledAt;this.cancellationReason=cancellationReason;this.createdAt=createdAt;this.files=List.copyOf(files);
    }
    public Long getId(){return id;} public String getTrackingNumber(){return trackingNumber;} public String getFullName(){return fullName;} public String getRegistrationNumber(){return registrationNumber;}
    public UserType getUserType(){return userType;} public String getDepartment(){return department;} public PrintColor getPrintColor(){return printColor;} public PrintingSide getPrintingSide(){return printingSide;}
    public int getCopies(){return copies;} public PrintRequestStatus getStatus(){return status;} public PaymentStatus getPaymentStatus(){return paymentStatus;} public Long getPricingConfigId(){return pricingConfigId;}
    public long getPricingRevision(){return pricingRevision;} public BigDecimal getAppliedRate(){return appliedRate;} public long getTotalDocumentPages(){return totalDocumentPages;} public long getTotalPhysicalSheets(){return totalPhysicalSheets;}
    public BigDecimal getTotalAmount(){return totalAmount;} public LocalDateTime getCalculatedAt(){return calculatedAt;} public LocalDateTime getExpiresAt(){return expiresAt;} public LocalDateTime getCancelledAt(){return cancelledAt;}
    public String getCancellationReason(){return cancellationReason;} public LocalDateTime getCreatedAt(){return createdAt;} public List<PrintFileResponse> getFiles(){return files;}
}