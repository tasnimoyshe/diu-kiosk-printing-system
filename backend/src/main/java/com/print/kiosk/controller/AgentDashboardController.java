package com.print.kiosk.controller;

import com.print.kiosk.dto.response.AgentDashboardResponse;
import com.print.kiosk.service.interfaces.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/agent/dashboard")
public class AgentDashboardController {

    private final DashboardService dashboardService;

    public AgentDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<AgentDashboardResponse> dashboard(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate referenceDate
    ) {
        return ResponseEntity.ok(
                dashboardService.agentDashboard(
                        accountId(jwt),
                        referenceDate
                )
        );
    }

    private Long accountId(Jwt jwt) {
        Number value = jwt.getClaim("accountId");
        if (value == null) {
            throw new IllegalArgumentException(
                    "Authenticated account ID is missing"
            );
        }
        return value.longValue();
    }
}