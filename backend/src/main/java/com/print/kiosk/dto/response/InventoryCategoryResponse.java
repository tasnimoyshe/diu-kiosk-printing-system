package com.print.kiosk.dto.response;

import java.time.LocalDateTime;

public record InventoryCategoryResponse(
        Long id,
        String name,
        boolean archived,
        boolean deleted,
        Long mergedIntoCategoryId,
        String mergedIntoCategoryName,
        long activeItemCount,
        long totalItemCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}