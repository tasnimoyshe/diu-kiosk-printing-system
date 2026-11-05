package com.print.kiosk.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AgentDashboardResponse(
        Long agentAccountId,
        String agentId,
        String fullName,
        long sharedActiveQueueCount,
        long sharedCompletedToday,
        long ownCompletedToday,
        long ownCompletedCurrentWeek,
        long ownCompletedCurrentMonth,
        BigDecimal ownAverageProcessingMinutesCurrentMonth,
        long ownPausedOrFailedCurrentMonth,
        long ownInventoryActionsToday,
        long ownInventoryActionsCurrentWeek,
        long ownInventoryActionsCurrentMonth,
        BigDecimal ownInventoryNetAmountToday,
        BigDecimal ownInventoryNetAmountCurrentWeek,
        BigDecimal ownInventoryNetAmountCurrentMonth,
        LocalDateTime generatedAt
) {
}