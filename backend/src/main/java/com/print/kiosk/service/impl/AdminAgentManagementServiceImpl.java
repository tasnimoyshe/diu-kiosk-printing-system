package com.print.kiosk.service.impl;

import com.print.kiosk.dto.response.AgentSummaryResponse;
import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.AgentStatus;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.exception.ResourceNotFoundException;
import com.print.kiosk.repository.AgentIdCardRepository;
import com.print.kiosk.repository.UserAccountRepository;
import com.print.kiosk.service.interfaces.AdminAgentManagementService;
import com.print.kiosk.service.interfaces.RefreshTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminAgentManagementServiceImpl
        implements AdminAgentManagementService {

    private final UserAccountRepository userAccountRepository;
    private final AgentIdCardRepository agentIdCardRepository;
    private final RefreshTokenService refreshTokenService;

    public AdminAgentManagementServiceImpl(
            UserAccountRepository userAccountRepository,
            AgentIdCardRepository agentIdCardRepository,
            RefreshTokenService refreshTokenService
    ) {
        this.userAccountRepository =
                userAccountRepository;

        this.agentIdCardRepository =
                agentIdCardRepository;

        this.refreshTokenService =
                refreshTokenService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentSummaryResponse>
    getPendingAgents() {
        List<UserAccount> pendingAgents =
                userAccountRepository
                        .findAllByRoleAndAgentStatusOrderByCreatedAtAsc(
                                AccountRole.AGENT,
                                AgentStatus.PENDING
                        );

        return pendingAgents.stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional
    public AgentSummaryResponse approveAgent(
            Long agentAccountId
    ) {
        UserAccount agent =
                getAgent(agentAccountId);

        requireStatus(
                agent,
                AgentStatus.PENDING,
                "Only pending agents can be approved"
        );

        agent.setAgentStatus(
                AgentStatus.APPROVED
        );

        return toSummaryResponse(
                userAccountRepository.save(agent)
        );
    }

    @Override
    @Transactional
    public AgentSummaryResponse rejectAgent(
            Long agentAccountId
    ) {
        UserAccount agent =
                getAgent(agentAccountId);

        requireStatus(
                agent,
                AgentStatus.PENDING,
                "Only pending agents can be rejected"
        );

        agent.setAgentStatus(
                AgentStatus.REJECTED
        );

        UserAccount savedAgent =
                userAccountRepository.save(agent);

        refreshTokenService.revokeAllForAccount(
                savedAgent.getId()
        );

        return toSummaryResponse(savedAgent);
    }

    @Override
    @Transactional
    public AgentSummaryResponse suspendAgent(
            Long agentAccountId
    ) {
        UserAccount agent =
                getAgent(agentAccountId);

        requireStatus(
                agent,
                AgentStatus.APPROVED,
                "Only approved agents can be suspended"
        );

        agent.setAgentStatus(
                AgentStatus.SUSPENDED
        );

        UserAccount savedAgent =
                userAccountRepository.save(agent);

        refreshTokenService.revokeAllForAccount(
                savedAgent.getId()
        );

        return toSummaryResponse(savedAgent);
    }

    @Override
    @Transactional
    public AgentSummaryResponse reactivateAgent(
            Long agentAccountId
    ) {
        UserAccount agent =
                getAgent(agentAccountId);

        requireStatus(
                agent,
                AgentStatus.SUSPENDED,
                "Only suspended agents can be reactivated"
        );

        agent.setAgentStatus(
                AgentStatus.APPROVED
        );

        return toSummaryResponse(
                userAccountRepository.save(agent)
        );
    }

    @Override
    @Transactional
    public AgentSummaryResponse deleteAgent(
            Long agentAccountId
    ) {
        UserAccount agent =
                getAgent(agentAccountId);

        if (agent.getAgentStatus()
                == AgentStatus.DELETED) {

            throw new IllegalArgumentException(
                    "Agent account is already deleted"
            );
        }

        agent.setAgentStatus(
                AgentStatus.DELETED
        );

        UserAccount savedAgent =
                userAccountRepository.save(agent);

        refreshTokenService.revokeAllForAccount(
                savedAgent.getId()
        );

        return toSummaryResponse(savedAgent);
    }

    private UserAccount getAgent(
            Long agentAccountId
    ) {
        UserAccount account =
                userAccountRepository
                        .findById(agentAccountId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Agent account was not found"
                                )
                        );

        if (account.getRole()
                != AccountRole.AGENT) {

            throw new ResourceNotFoundException(
                    "Agent account was not found"
            );
        }

        return account;
    }

    private void requireStatus(
            UserAccount agent,
            AgentStatus expectedStatus,
            String message
    ) {
        if (agent.getAgentStatus()
                != expectedStatus) {

            throw new IllegalArgumentException(
                    message
            );
        }
    }

    private AgentSummaryResponse toSummaryResponse(
            UserAccount account
    ) {
        boolean idCardAvailable =
                agentIdCardRepository
                        .existsByUserAccountId(
                                account.getId()
                        );

        return new AgentSummaryResponse(
                account.getId(),
                account.getFullName(),
                account.getAgentId(),
                account.getEmail(),
                account.getContactNumber(),
                account.getAgentStatus(),
                account.getCreatedAt(),
                idCardAvailable
        );
    }
}