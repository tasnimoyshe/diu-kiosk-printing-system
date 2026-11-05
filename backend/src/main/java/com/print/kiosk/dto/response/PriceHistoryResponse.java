package com.print.kiosk.dto.response;

import com.print.kiosk.entity.PriceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PriceHistoryResponse {

    private final Long id;
    private final PriceType priceType;
    private final BigDecimal previousPrice;
    private final BigDecimal newPrice;
    private final long pricingRevision;
    private final Long changedByAccountId;
    private final String changedByAgentId;
    private final LocalDateTime changedAt;

    public PriceHistoryResponse(
            Long id,
            PriceType priceType,
            BigDecimal previousPrice,
            BigDecimal newPrice,
            long pricingRevision,
            Long changedByAccountId,
            String changedByAgentId,
            LocalDateTime changedAt
    ) {
        this.id = id;
        this.priceType = priceType;
        this.previousPrice = previousPrice;
        this.newPrice = newPrice;
        this.pricingRevision = pricingRevision;
        this.changedByAccountId = changedByAccountId;
        this.changedByAgentId = changedByAgentId;
        this.changedAt = changedAt;
    }

    public Long getId() {
        return id;
    }

    public PriceType getPriceType() {
        return priceType;
    }

    public BigDecimal getPreviousPrice() {
        return previousPrice;
    }

    public BigDecimal getNewPrice() {
        return newPrice;
    }

    public long getPricingRevision() {
        return pricingRevision;
    }

    public Long getChangedByAccountId() {
        return changedByAccountId;
    }

    public String getChangedByAgentId() {
        return changedByAgentId;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}