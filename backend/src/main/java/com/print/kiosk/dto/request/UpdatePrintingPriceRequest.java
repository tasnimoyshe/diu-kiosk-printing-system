package com.print.kiosk.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class UpdatePrintingPriceRequest {

    @NotNull(message = "Current pricing revision is required")
    private Long revision;

    @NotNull(message = "Color single-sided price is required")
    @DecimalMin(value = "0.01", message = "Color single-sided price must be greater than zero")
    @DecimalMax(value = "10000.00", message = "Color single-sided price is too large")
    @Digits(integer = 10, fraction = 2, message = "Color single-sided price may contain at most two decimal places")
    private BigDecimal colorSingleSided;

    @NotNull(message = "Black-and-white single-sided price is required")
    @DecimalMin(value = "0.01", message = "Black-and-white single-sided price must be greater than zero")
    @DecimalMax(value = "10000.00", message = "Black-and-white single-sided price is too large")
    @Digits(integer = 10, fraction = 2, message = "Black-and-white single-sided price may contain at most two decimal places")
    private BigDecimal blackWhiteSingleSided;

    @NotNull(message = "Color double-sided price is required")
    @DecimalMin(value = "0.01", message = "Color double-sided price must be greater than zero")
    @DecimalMax(value = "10000.00", message = "Color double-sided price is too large")
    @Digits(integer = 10, fraction = 2, message = "Color double-sided price may contain at most two decimal places")
    private BigDecimal colorDoubleSided;

    @NotNull(message = "Black-and-white double-sided price is required")
    @DecimalMin(value = "0.01", message = "Black-and-white double-sided price must be greater than zero")
    @DecimalMax(value = "10000.00", message = "Black-and-white double-sided price is too large")
    @Digits(integer = 10, fraction = 2, message = "Black-and-white double-sided price may contain at most two decimal places")
    private BigDecimal blackWhiteDoubleSided;

    public Long getRevision() {
        return revision;
    }

    public void setRevision(Long revision) {
        this.revision = revision;
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
}