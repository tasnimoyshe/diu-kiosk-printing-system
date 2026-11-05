package com.print.kiosk.dto.response;

import java.math.BigDecimal;

public record RevenueWindowResponse(
        BigDecimal today,
        BigDecimal currentWeek,
        BigDecimal currentMonth
) {
}