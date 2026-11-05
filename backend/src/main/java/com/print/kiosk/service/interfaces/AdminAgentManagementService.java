package com.print.kiosk.service.interfaces;

import com.print.kiosk.dto.response.AgentSummaryResponse;

import java.util.List;

public interface AdminAgentManagementService {

    List<AgentSummaryResponse> getPendingAgents();

    AgentSummaryResponse approveAgent(
            Long agentAccountId
    );

    AgentSummaryResponse rejectAgent(
            Long agentAccountId
    );

    AgentSummaryResponse suspendAgent(
            Long agentAccountId
    );

    AgentSummaryResponse reactivateAgent(
            Long agentAccountId
    );

    AgentSummaryResponse deleteAgent(
            Long agentAccountId
    );
}