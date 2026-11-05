package com.print.kiosk.controller;

import com.print.kiosk.dto.response.AdminDashboardResponse;
import com.print.kiosk.dto.response.AdminReportResponse;
import com.print.kiosk.dto.response.AgentPerformanceResponse;
import com.print.kiosk.entity.ReportingPeriod;
import com.print.kiosk.service.interfaces.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> dashboard(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate referenceDate
    ) {
        return ResponseEntity.ok(
                dashboardService.adminDashboard(referenceDate)
        );
    }

    @GetMapping("/reports")
    public ResponseEntity<AdminReportResponse> report(
            @RequestParam ReportingPeriod period,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate referenceDate
    ) {
        return ResponseEntity.ok(
                dashboardService.adminReport(period, referenceDate)
        );
    }

    @GetMapping("/agents/performance")
    public ResponseEntity<List<AgentPerformanceResponse>> performance(
            @RequestParam ReportingPeriod period,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate referenceDate
    ) {
        return ResponseEntity.ok(
                dashboardService.agentPerformance(period, referenceDate)
        );
    }
}