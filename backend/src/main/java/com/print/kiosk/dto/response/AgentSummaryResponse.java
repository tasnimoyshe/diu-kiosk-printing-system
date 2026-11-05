package com.print.kiosk.dto.response;

import com.print.kiosk.entity.AgentStatus;

import java.time.LocalDateTime;

public class AgentSummaryResponse {

    private final Long id;
    private final String fullName;
    private final String agentId;
    private final String email;
    private final String contactNumber;
    private final AgentStatus status;
    private final LocalDateTime createdAt;
    private final boolean idCardAvailable;

    public AgentSummaryResponse(
            Long id,
            String fullName,
            String agentId,
            String email,
            String contactNumber,
            AgentStatus status,
            LocalDateTime createdAt,
            boolean idCardAvailable
    ) {
        this.id = id;
        this.fullName = fullName;
        this.agentId = agentId;
        this.email = email;
        this.contactNumber = contactNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.idCardAvailable = idCardAvailable;
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

    public AgentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isIdCardAvailable() {
        return idCardAvailable;
    }
}