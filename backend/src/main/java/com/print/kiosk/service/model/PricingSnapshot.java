package com.print.kiosk.service.model;

import com.print.kiosk.entity.PrintColor;
import com.print.kiosk.entity.PrintingSide;

import java.math.BigDecimal;

public record PricingSnapshot(
        Long pricingConfigId,
        long revision,
        BigDecimal colorSingleSided,
        BigDecimal blackWhiteSingleSided,
        BigDecimal colorDoubleSided,
        BigDecimal blackWhiteDoubleSided
) {
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
        throw new IllegalArgumentException("Unsupported printing selection");
    }
}