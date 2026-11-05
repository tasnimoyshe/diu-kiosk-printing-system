package com.print.kiosk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(
        name = "printing_prices",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_printing_prices_config_key",
                columnNames = "config_key"
        )
)
public class PrintingPrice {

    private static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, length = 30)
    private String configKey;

    @Column(name = "color_single_sided", nullable = false, precision = 12, scale = 2)
    private BigDecimal colorSingleSided;

    @Column(name = "black_white_single_sided", nullable = false, precision = 12, scale = 2)
    private BigDecimal blackWhiteSingleSided;

    @Column(name = "color_double_sided", nullable = false, precision = 12, scale = 2)
    private BigDecimal colorDoubleSided;

    @Column(name = "black_white_double_sided", nullable = false, precision = 12, scale = 2)
    private BigDecimal blackWhiteDoubleSided;

    @Column(name = "revision", nullable = false)
    private long revision;

    @Column(name = "updated_by_account_id")
    private Long updatedByAccountId;

    @Column(name = "updated_by_agent_id", length = 50)
    private String updatedByAgentId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "row_version", nullable = false)
    private Long rowVersion;

    @PrePersist
    public void beforeInsert() {
        LocalDateTime now = LocalDateTime.now(DHAKA_ZONE);
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (revision <= 0) {
            revision = 1;
        }
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now(DHAKA_ZONE);
    }

    public BigDecimal priceFor(PrintColor color, PrintingSide side) {
        if (color == PrintColor.COLOR && side == PrintingSide.SINGLE_SIDED) {
            return colorSingleSided;
        }
        if (color == PrintColor.BLACK_AND_WHITE && side == PrintingSide.SINGLE_SIDED) {
            return blackWhiteSingleSided;
        }
        if (color == PrintColor.COLOR && side == PrintingSide.DOUBLE_SIDED) {
            return colorDoubleSided;
        }
        if (color == PrintColor.BLACK_AND_WHITE && side == PrintingSide.DOUBLE_SIDED) {
            return blackWhiteDoubleSided;
        }
        throw new IllegalArgumentException("Unsupported printing price selection");
    }

    public Long getId() {
        return id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public BigDecimal getColorSingleSided() {
        return colorSingleSided;
    }

    public void setColorSingleSided(BigDecimal colorSingleSided) {
        this.colorSingleSided = colorSingleSided;
    }

    public BigDecimal getBlackWhiteSingleSided() {
        return blackWhiteSingleSided;
    }

    public void setBlackWhiteSingleSided(BigDecimal blackWhiteSingleSided) {
        this.blackWhiteSingleSided = blackWhiteSingleSided;
    }

    public BigDecimal getColorDoubleSided() {
        return colorDoubleSided;
    }

    public void setColorDoubleSided(BigDecimal colorDoubleSided) {
        this.colorDoubleSided = colorDoubleSided;
    }

    public BigDecimal getBlackWhiteDoubleSided() {
        return blackWhiteDoubleSided;
    }

    public void setBlackWhiteDoubleSided(BigDecimal blackWhiteDoubleSided) {
        this.blackWhiteDoubleSided = blackWhiteDoubleSided;
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }

    public Long getUpdatedByAccountId() {
        return updatedByAccountId;
    }

    public void setUpdatedByAccountId(Long updatedByAccountId) {
        this.updatedByAccountId = updatedByAccountId;
    }

    public String getUpdatedByAgentId() {
        return updatedByAgentId;
    }

    public void setUpdatedByAgentId(String updatedByAgentId) {
        this.updatedByAgentId = updatedByAgentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getRowVersion() {
        return rowVersion;
    }
}