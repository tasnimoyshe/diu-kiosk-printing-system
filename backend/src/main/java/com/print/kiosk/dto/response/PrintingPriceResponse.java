package com.print.kiosk.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PrintingPriceResponse {

    private final Long id;
    private final long revision;
    private final BigDecimal colorSingleSided;
    private final BigDecimal blackWhiteSingleSided;
    private final BigDecimal colorDoubleSided;
    private final BigDecimal blackWhiteDoubleSided;
    private final String updatedByAgentId;
    private final LocalDateTime updatedAt;

    public PrintingPriceResponse(
            Long id,
            long revision,
            BigDecimal colorSingleSided,
            BigDecimal blackWhiteSingleSided,
            BigDecimal colorDoubleSided,
            BigDecimal blackWhiteDoubleSided,
            String updatedByAgentId,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.revision = revision;
        this.colorSingleSided = colorSingleSided;
        this.blackWhiteSingleSided = blackWhiteSingleSided;
        this.colorDoubleSided = colorDoubleSided;
        this.blackWhiteDoubleSided = blackWhiteDoubleSided;
        this.updatedByAgentId = updatedByAgentId;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public long getRevision() {
        return revision;
    }

    public BigDecimal getColorSingleSided() {
        return colorSingleSided;
    }

    public BigDecimal getBlackWhiteSingleSided() {
        return blackWhiteSingleSided;
    }

    public BigDecimal getColorDoubleSided() {
        return colorDoubleSided;
    }

    public BigDecimal getBlackWhiteDoubleSided() {
        return blackWhiteDoubleSided;
    }

    public String getUpdatedByAgentId() {
        return updatedByAgentId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}