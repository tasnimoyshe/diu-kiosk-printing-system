package com.print.kiosk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "cash_payment_pins", indexes = {
        @Index(name = "idx_cash_pins_request_created", columnList = "print_request_id, created_at"),
        @Index(name = "idx_cash_pins_hash_active", columnList = "pin_hash, expires_at, used_at, revoked_at")
})
public class CashPaymentPin {
    private static final ZoneId DHAKA_ZONE=ZoneId.of("Asia/Dhaka");
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="print_request_id", nullable=false) private PrintRequest printRequest;
    @OneToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="payment_record_id", nullable=false, unique=true) private PaymentRecord paymentRecord;
    @Column(name="pin_hash", nullable=false, length=64) private String pinHash;
    @Column(name="created_at", nullable=false, updatable=false) private LocalDateTime createdAt;
    @Column(name="expires_at", nullable=false) private LocalDateTime expiresAt;
    @Column(name="used_at") private LocalDateTime usedAt;
    @Column(name="revoked_at") private LocalDateTime revokedAt;
    @Column(name="failed_attempts", nullable=false) private int failedAttempts;
    @Column(name="blocked_until") private LocalDateTime blockedUntil;
    @Version @Column(name="version", nullable=false) private Long version;
    @PrePersist public void beforeInsert(){if(createdAt==null)createdAt=LocalDateTime.now(DHAKA_ZONE);}
    public Long getId(){return id;} public PrintRequest getPrintRequest(){return printRequest;} public void setPrintRequest(PrintRequest v){printRequest=v;}
    public PaymentRecord getPaymentRecord(){return paymentRecord;} public void setPaymentRecord(PaymentRecord v){paymentRecord=v;}
    public String getPinHash(){return pinHash;} public void setPinHash(String v){pinHash=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getExpiresAt(){return expiresAt;} public void setExpiresAt(LocalDateTime v){expiresAt=v;}
    public LocalDateTime getUsedAt(){return usedAt;} public void setUsedAt(LocalDateTime v){usedAt=v;}
    public LocalDateTime getRevokedAt(){return revokedAt;} public void setRevokedAt(LocalDateTime v){revokedAt=v;}
    public int getFailedAttempts(){return failedAttempts;} public void setFailedAttempts(int v){failedAttempts=v;}
    public LocalDateTime getBlockedUntil(){return blockedUntil;} public void setBlockedUntil(LocalDateTime v){blockedUntil=v;}
    public Long getVersion(){return version;}
    public boolean isActive(LocalDateTime now){return usedAt==null&&revokedAt==null&&expiresAt!=null&&expiresAt.isAfter(now);}
}