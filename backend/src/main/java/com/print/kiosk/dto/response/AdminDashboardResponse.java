package com.print.kiosk.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record AdminDashboardResponse(
        long activeQueueCount,
        long completedToday,
        PrintStatisticsResponse todayPrintStatistics,
        RevenueWindowResponse printingRevenue,
        RevenueWindowResponse inventoryRevenue,
        List<SalesBreakdownResponse> currentMonthCategorySales,
        List<SalesBreakdownResponse> currentMonthItemSales,
        List<AgentPerformanceResponse> currentMonthAgentPerformance,
        LocalDateTime generatedAt
) {
}