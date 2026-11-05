package com.print.kiosk.dto.response;

import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.AgentStatus;

import java.time.LocalDateTime;

public class AgentRegistrationResponse {

    private final Long id;
    private final String fullName;
    private final String agentId;
    private final String email;
    private final String contactNumber;
    private final AccountRole role;
    private final AgentStatus status;
    private final LocalDateTime createdAt;
    private final String message;

    public AgentRegistrationResponse(
            Long id,
            String fullName,
            String agentId,
            String email,
            String contactNumber,
            AccountRole role,
            AgentStatus status,
            LocalDateTime createdAt,
            String message
    ) {
        this.id = id;
        this.fullName = fullName;
        this.agentId = agentId;
        this.email = email;
        this.contactNumber = contactNumber;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.message = message;
    }

    public Long getId() {
        return id;
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

    public String getContactNumber() {
        return contactNumber;
    }

    public AccountRole getRole() {
        return role;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getMessage() {
        return message;
    }
}