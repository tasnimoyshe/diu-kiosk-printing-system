package com.print.kiosk.controller;

import com.print.kiosk.dto.response.AgentSummaryResponse;
import com.print.kiosk.service.interfaces.AdminAgentManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/agents")
public class AdminAgentManagementController {

    private final AdminAgentManagementService
            adminAgentManagementService;

    public AdminAgentManagementController(
            AdminAgentManagementService
                    adminAgentManagementService
    ) {
        this.adminAgentManagementService =
                adminAgentManagementService;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<AgentSummaryResponse>>
    getPendingAgents() {
        return ResponseEntity.ok(
                adminAgentManagementService
                        .getPendingAgents()
        );
    }

    @PatchMapping("/{agentAccountId}/approve")
    public ResponseEntity<AgentSummaryResponse>
    approveAgent(
            @PathVariable Long agentAccountId
    ) {
        return ResponseEntity.ok(
                adminAgentManagementService
                        .approveAgent(agentAccountId)
        );
    }

    @PatchMapping("/{agentAccountId}/reject")
    public ResponseEntity<AgentSummaryResponse>
    rejectAgent(
            @PathVariable Long agentAccountId
    ) {
        return ResponseEntity.ok(
                adminAgentManagementService
                        .rejectAgent(agentAccountId)
        );
    }

    @PatchMapping("/{agentAccountId}/suspend")
    public ResponseEntity<AgentSummaryResponse>
    suspendAgent(
            @PathVariable Long agentAccountId
    ) {
        return ResponseEntity.ok(
                adminAgentManagementService
                        .suspendAgent(agentAccountId)
        );
    }

    @PatchMapping("/{agentAccountId}/reactivate")
    public ResponseEntity<AgentSummaryResponse>
    reactivateAgent(
            @PathVariable Long agentAccountId
    ) {
        return ResponseEntity.ok(
                adminAgentManagementService
                        .reactivateAgent(agentAccountId)
        );
    }

    @DeleteMapping("/{agentAccountId}")
    public ResponseEntity<AgentSummaryResponse>
    deleteAgent(
            @PathVariable Long agentAccountId
    ) {
        return ResponseEntity.ok(
                adminAgentManagementService
                        .deleteAgent(agentAccountId)
        );
    }
}