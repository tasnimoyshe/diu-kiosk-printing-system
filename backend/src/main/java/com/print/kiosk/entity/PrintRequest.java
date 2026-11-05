package com.print.kiosk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(
        name = "print_requests",
        indexes = {
                @Index(name = "idx_print_requests_status_payment_created", columnList = "status, payment_status, created_at"),
                @Index(name = "idx_print_requests_expires_at", columnList = "expires_at"),
                @Index(name = "idx_print_requests_assigned_agent", columnList = "assigned_agent_id"),
                @Index(name = "idx_print_requests_claim_activity", columnList = "claim_last_activity_at")
        }
)
public class PrintRequest {

    private static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 40)
    private String trackingNumber;

    @Column(name = "full_name", nullable = false, length = 40)
    private String fullName;

    @Column(name = "registration_number", nullable = false, length = 30)
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType;

    @Column(name = "department", length = 100)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "print_color", nullable = false, length = 30)
    private PrintColor printColor;

    @Enumerated(EnumType.STRING)
    @Column(name = "printing_side", nullable = false, length = 30)
    private PrintingSide printingSide;

    @Column(name = "copies", nullable = false)
    private int copies;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PrintRequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id")
    private UserAccount assignedAgent;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @Column(name = "claim_last_activity_at")
    private LocalDateTime claimLastActivityAt;

    @Column(name = "printing_started_at")
    private LocalDateTime printingStartedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "reopened_at")
    private LocalDateTime reopenedAt;

    @Column(name = "pricing_config_id", nullable = false)
    private Long pricingConfigId;

    @Column(name = "pricing_revision", nullable = false)
    private long pricingRevision;

    @Column(name = "applied_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal appliedRate;

    @Column(name = "total_document_pages", nullable = false)
    private long totalDocumentPages;

    @Column(name = "total_physical_sheets", nullable = false)
    private long totalPhysicalSheets;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 200)
    private String cancellationReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @PrePersist
    public void beforeInsert() {
        LocalDateTime now = LocalDateTime.now(DHAKA_ZONE);
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (status == null) status = PrintRequestStatus.AWAITING_PAYMENT;
        if (paymentStatus == null) paymentStatus = PaymentStatus.PENDING;
        if (totalAmount == null) totalAmount = BigDecimal.ZERO.setScale(2);
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now(DHAKA_ZONE);
    }

    public Long getId() { return id; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public PrintColor getPrintColor() { return printColor; }
    public void setPrintColor(PrintColor printColor) { this.printColor = printColor; }
    public PrintingSide getPrintingSide() { return printingSide; }
    public void setPrintingSide(PrintingSide printingSide) { this.printingSide = printingSide; }
    public int getCopies() { return copies; }
    public void setCopies(int copies) { this.copies = copies; }
    public PrintRequestStatus getStatus() { return status; }
    public void setStatus(PrintRequestStatus status) { this.status = status; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public UserAccount getAssignedAgent() { return assignedAgent; }
    public void setAssignedAgent(UserAccount assignedAgent) { this.assignedAgent = assignedAgent; }
    public LocalDateTime getClaimedAt() { return claimedAt; }
    public void setClaimedAt(LocalDateTime claimedAt) { this.claimedAt = claimedAt; }
    public LocalDateTime getClaimLastActivityAt() { return claimLastActivityAt; }
    public void setClaimLastActivityAt(LocalDateTime claimLastActivityAt) { this.claimLastActivityAt = claimLastActivityAt; }
    public LocalDateTime getPrintingStartedAt() { return printingStartedAt; }
    public void setPrintingStartedAt(LocalDateTime printingStartedAt) { this.printingStartedAt = printingStartedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getPausedAt() { return pausedAt; }
    public void setPausedAt(LocalDateTime pausedAt) { this.pausedAt = pausedAt; }
    public LocalDateTime getReopenedAt() { return reopenedAt; }
    public void setReopenedAt(LocalDateTime reopenedAt) { this.reopenedAt = reopenedAt; }
    public Long getPricingConfigId() { return pricingConfigId; }
    public void setPricingConfigId(Long pricingConfigId) { this.pricingConfigId = pricingConfigId; }
    public long getPricingRevision() { return pricingRevision; }
    public void setPricingRevision(long pricingRevision) { this.pricingRevision = pricingRevision; }
    public BigDecimal getAppliedRate() { return appliedRate; }
    public void setAppliedRate(BigDecimal appliedRate) { this.appliedRate = appliedRate; }
    public long getTotalDocumentPages() { return totalDocumentPages; }
    public void setTotalDocumentPages(long totalDocumentPages) { this.totalDocumentPages = totalDocumentPages; }
    public long getTotalPhysicalSheets() { return totalPhysicalSheets; }
    public void setTotalPhysicalSheets(long totalPhysicalSheets) { this.totalPhysicalSheets = totalPhysicalSheets; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}