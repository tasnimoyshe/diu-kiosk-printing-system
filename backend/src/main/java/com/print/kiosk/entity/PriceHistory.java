package com.print.kiosk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "price_history")
public class PriceHistory {

    private static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false, length = 40)
    private PriceType priceType;

    @Column(name = "previous_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal previousPrice;

    @Column(name = "new_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal newPrice;

    @Column(name = "pricing_revision", nullable = false)
    private long pricingRevision;

    @Column(name = "changed_by_account_id", nullable = false)
    private Long changedByAccountId;

    @Column(name = "changed_by_agent_id", nullable = false, length = 50)
    private String changedByAgentId;

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

    public PriceType getPriceType() {
        return priceType;
    }

    public void setPriceType(PriceType priceType) {
        this.priceType = priceType;
    }

    public BigDecimal getPreviousPrice() {
        return previousPrice;
    }

    public void setPreviousPrice(BigDecimal previousPrice) {
        this.previousPrice = previousPrice;
    }

    public BigDecimal getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(BigDecimal newPrice) {
        this.newPrice = newPrice;
    }

    public long getPricingRevision() {
        return pricingRevision;
    }

    public void setPricingRevision(long pricingRevision) {
        this.pricingRevision = pricingRevision;
    }

    public Long getChangedByAccountId() {
        return changedByAccountId;
    }

    public void setChangedByAccountId(Long changedByAccountId) {
        this.changedByAccountId = changedByAccountId;
    }

    public String getChangedByAgentId() {
        return changedByAgentId;
    }

    public void setChangedByAgentId(String changedByAgentId) {
        this.changedByAgentId = changedByAgentId;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}