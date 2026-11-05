package com.print.kiosk.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "payment_records", indexes = {
        @Index(name = "idx_payment_records_request", columnList = "print_request_id"),
        @Index(name = "idx_payment_records_status_created", columnList = "status, created_at")
})
public class PaymentRecord {
    private static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "print_request_id", nullable = false) private PrintRequest printRequest;
    @Enumerated(EnumType.STRING) @Column(name = "payment_method", nullable = false, length = 20) private PaymentMethod paymentMethod;
    @Column(name = "amount", nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 20) private PaymentStatus status;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "completed_at") private LocalDateTime completedAt;
    @Column(name = "failure_reason", length = 255) private String failureReason;
    @Column(name = "external_transaction_id", length = 120) private String externalTransactionId;
    @Version @Column(name = "version", nullable = false) private Long version;
    @PrePersist public void beforeInsert(){ if(createdAt==null) createdAt=LocalDateTime.now(DHAKA_ZONE); if(status==null) status=PaymentStatus.PENDING; }
    public Long getId(){return id;} public PrintRequest getPrintRequest(){return printRequest;} public void setPrintRequest(PrintRequest v){printRequest=v;}
    public PaymentMethod getPaymentMethod(){return paymentMethod;} public void setPaymentMethod(PaymentMethod v){paymentMethod=v;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
    public PaymentStatus getStatus(){return status;} public void setStatus(PaymentStatus v){status=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getCompletedAt(){return completedAt;} public void setCompletedAt(LocalDateTime v){completedAt=v;}
    public String getFailureReason(){return failureReason;} public void setFailureReason(String v){failureReason=v;}
    public String getExternalTransactionId(){return externalTransactionId;} public void setExternalTransactionId(String v){externalTransactionId=v;}
    public Long getVersion(){return version;}
}