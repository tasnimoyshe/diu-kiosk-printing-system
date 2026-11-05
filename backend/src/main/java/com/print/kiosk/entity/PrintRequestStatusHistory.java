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
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(
        name = "print_request_status_history",
        indexes = @Index(
                name = "idx_print_request_status_history_request_changed",
                columnList = "print_request_id, changed_at"
        )
)
public class PrintRequestStatusHistory {

    private static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "print_request_id", nullable = false)
    private PrintRequest printRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private PrintRequestStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private PrintRequestStatus newStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 20)
    private StatusActorType actorType;

    @Column(name = "actor_account_id")
    private Long actorAccountId;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @PrePersist
    public void beforeInsert() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now(DHAKA_ZONE);
        }
    }

    public Long getId() {
        return id;
    }

    public PrintRequest getPrintRequest() {
        return printRequest;
    }

    public void setPrintRequest(PrintRequest printRequest) {
        this.printRequest = printRequest;
    }

    public PrintRequestStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(PrintRequestStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public PrintRequestStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(PrintRequestStatus newStatus) {
        this.newStatus = newStatus;
    }

    public StatusActorType getActorType() {
        return actorType;
    }

    public void setActorType(StatusActorType actorType) {
        this.actorType = actorType;
    }

    public Long getActorAccountId() {
        return actorAccountId;
    }

    public void setActorAccountId(Long actorAccountId) {
        this.actorAccountId = actorAccountId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}