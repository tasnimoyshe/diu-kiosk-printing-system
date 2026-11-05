package com.print.kiosk.controller;

import com.print.kiosk.dto.request.AgentRegistrationRequest;
import com.print.kiosk.dto.response.AgentRegistrationResponse;
import com.print.kiosk.service.interfaces.AgentRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/agents")
public class AgentRegistrationController {

    private final AgentRegistrationService agentRegistrationService;

    public AgentRegistrationController(
            AgentRegistrationService agentRegistrationService
    ) {
        this.agentRegistrationService = agentRegistrationService;
    }

    @PostMapping(
            value = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<AgentRegistrationResponse> registerAgent(
            @Valid @ModelAttribute AgentRegistrationRequest request
    ) {
        AgentRegistrationResponse response =
                agentRegistrationService.registerAgent(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}