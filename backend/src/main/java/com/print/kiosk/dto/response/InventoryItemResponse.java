package com.print.kiosk.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryItemResponse(
        Long id,
        String name,
        Long categoryId,
        String categoryName,
        BigDecimal currentPrice,
        boolean archived,
        Long createdByAgentAccountId,
        String createdByAgentId,
        long netSoldQuantity,
        BigDecimal netRevenue,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}