package com.print.kiosk.dto.response;

import com.print.kiosk.entity.ReportingPeriod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminReportResponse(
        ReportingPeriod period,
        LocalDateTime periodStart,
        LocalDateTime periodEndExclusive,
        long activeQueueCount,
        long completedRequests,
        PrintStatisticsResponse printStatistics,
        BigDecimal printingRevenue,
        BigDecimal inventoryRevenue,
        List<SalesBreakdownResponse> categorySales,
        List<SalesBreakdownResponse> itemSales,
        List<AgentPerformanceResponse> agentPerformance,
        LocalDateTime generatedAt
) {
}