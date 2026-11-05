package com.print.kiosk.service.interfaces;

import com.print.kiosk.dto.response.AdminDashboardResponse;
import com.print.kiosk.dto.response.AdminReportResponse;
import com.print.kiosk.dto.response.AgentDashboardResponse;
import com.print.kiosk.dto.response.AgentPerformanceResponse;
import com.print.kiosk.entity.ReportingPeriod;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    AdminDashboardResponse adminDashboard(LocalDate referenceDate);

    AdminReportResponse adminReport(
            ReportingPeriod period,
            LocalDate referenceDate
    );

    List<AgentPerformanceResponse> agentPerformance(
            ReportingPeriod period,
            LocalDate referenceDate
    );

    AgentDashboardResponse agentDashboard(
            Long agentAccountId,
            LocalDate referenceDate
    );
}