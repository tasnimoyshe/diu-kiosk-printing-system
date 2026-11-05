package com.print.kiosk.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryItemPriceHistoryResponse(
        Long id,
        Long itemId,
        BigDecimal previousPrice,
        BigDecimal newPrice,
        Long changedByAgentAccountId,
        String changedByAgentId,
        LocalDateTime changedAt
) {
}