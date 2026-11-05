package com.print.kiosk.dto.response;

import com.print.kiosk.entity.InventoryTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryTransactionResponse(
        Long id,
        Long itemId,
        String itemName,
        Long categoryIdAtTransaction,
        String categoryNameAtTransaction,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        InventoryTransactionType transactionType,
        Long agentAccountId,
        String agentId,
        String agentName,
        String reason,
        LocalDateTime transactedAt
) {
}