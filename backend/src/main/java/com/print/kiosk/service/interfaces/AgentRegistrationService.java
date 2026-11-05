package com.print.kiosk.service.interfaces;

import com.print.kiosk.dto.request.AgentRegistrationRequest;
import com.print.kiosk.dto.response.AgentRegistrationResponse;

public interface AgentRegistrationService {

    AgentRegistrationResponse registerAgent(
            AgentRegistrationRequest request
    );
}