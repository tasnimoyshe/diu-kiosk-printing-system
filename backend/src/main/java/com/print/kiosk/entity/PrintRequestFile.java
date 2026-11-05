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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "print_request_files", indexes = {
        @Index(name = "idx_print_request_files_request", columnList = "print_request_id"),
        @Index(name = "idx_print_request_files_deletion", columnList = "deletion_eligible_at, binary_deleted_at")
})
public class PrintRequestFile {
    private static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "print_request_id", nullable = false) private PrintRequest printRequest;
    @Column(name = "original_file_name", nullable = false, length = 255) private String originalFileName;
    @Enumerated(EnumType.STRING) @Column(name = "file_type", nullable = false, length = 20) private PrintFileType fileType;
    @Column(name = "mime_type", nullable = false, length = 100) private String mimeType;
    @Column(name = "file_size_bytes", nullable = false) private long fileSizeBytes;
    @Column(name = "page_count", nullable = false) private int pageCount;
    @Column(name = "physical_sheets_per_copy", nullable = false) private int physicalSheetsPerCopy;
    @Column(name = "total_physical_sheets", nullable = false) private long totalPhysicalSheets;
    @Column(name = "copies", nullable = false) private int copies;
    @Column(name = "applied_rate", nullable = false, precision = 12, scale = 2) private BigDecimal appliedRate;
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2) private BigDecimal subtotal;
    @Column(name = "calculated_at", nullable = false) private LocalDateTime calculatedAt;
    @Column(name = "uploaded_at", nullable = false, updatable = false) private LocalDateTime uploadedAt;
    @Column(name = "deletion_eligible_at") private LocalDateTime deletionEligibleAt;
    @Column(name = "binary_deleted_at") private LocalDateTime binaryDeletedAt;
    @Lob @Column(name = "file_data", columnDefinition = "LONGBLOB") private byte[] fileData;
    @PrePersist public void beforeInsert() { if (uploadedAt == null) uploadedAt = LocalDateTime.now(DHAKA_ZONE); }
    public Long getId() { return id; }
    public PrintRequest getPrintRequest() { return printRequest; }
    public void setPrintRequest(PrintRequest printRequest) { this.printRequest = printRequest; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    public PrintFileType getFileType() { return fileType; }
    public void setFileType(PrintFileType fileType) { this.fileType = fileType; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }
    public int getPhysicalSheetsPerCopy() { return physicalSheetsPerCopy; }
    public void setPhysicalSheetsPerCopy(int physicalSheetsPerCopy) { this.physicalSheetsPerCopy = physicalSheetsPerCopy; }
    public long getTotalPhysicalSheets() { return totalPhysicalSheets; }
    public void setTotalPhysicalSheets(long totalPhysicalSheets) { this.totalPhysicalSheets = totalPhysicalSheets; }
    public int getCopies() { return copies; }
    public void setCopies(int copies) { this.copies = copies; }
    public BigDecimal getAppliedRate() { return appliedRate; }
    public void setAppliedRate(BigDecimal appliedRate) { this.appliedRate = appliedRate; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public LocalDateTime getDeletionEligibleAt() { return deletionEligibleAt; }
    public void setDeletionEligibleAt(LocalDateTime deletionEligibleAt) { this.deletionEligibleAt = deletionEligibleAt; }
    public LocalDateTime getBinaryDeletedAt() { return binaryDeletedAt; }
    public void setBinaryDeletedAt(LocalDateTime binaryDeletedAt) { this.binaryDeletedAt = binaryDeletedAt; }
    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }
}