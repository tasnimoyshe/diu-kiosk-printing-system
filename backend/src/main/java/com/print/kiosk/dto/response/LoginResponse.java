package com.print.kiosk.dto.response;

import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.AgentStatus;

public class LoginResponse {

    private final Long accountId;
    private final String fullName;
    private final String agentId;
    private final String email;
    private final AccountRole role;
    private final AgentStatus agentStatus;
    private final String message;

    public LoginResponse(
            Long accountId,
            String fullName,
            String agentId,
            String email,
            AccountRole role,
            AgentStatus agentStatus,
            String message
    ) {
        this.accountId = accountId;
        this.fullName = fullName;
        this.agentId = agentId;
        this.email = email;
        this.role = role;
        this.agentStatus = agentStatus;
        this.message = message;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getEmail() {
        return email;
    }

    public AccountRole getRole() {
        return role;
    }

    public AgentStatus getAgentStatus() {
        return agentStatus;
    }

    public String getMessage() {
        return message;
    }
}