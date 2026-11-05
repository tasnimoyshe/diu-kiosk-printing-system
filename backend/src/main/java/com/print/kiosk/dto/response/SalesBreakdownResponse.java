package com.print.kiosk.dto.response;

import java.math.BigDecimal;

public record SalesBreakdownResponse(
        String label,
        long netQuantity,
        BigDecimal netAmount
) {
}