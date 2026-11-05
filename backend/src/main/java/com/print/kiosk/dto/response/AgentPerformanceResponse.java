package com.print.kiosk.dto.response;

import java.math.BigDecimal;

public record AgentPerformanceResponse(
        Long agentAccountId,
        String agentId,
        String fullName,
        long completedJobs,
        BigDecimal averageProcessingMinutes,
        long pausedOrFailedRequests,
        long inventoryActions,
        BigDecimal inventoryNetAmount
) {
}