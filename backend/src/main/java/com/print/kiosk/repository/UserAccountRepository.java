package com.print.kiosk.repository;

import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.AgentStatus;
import com.print.kiosk.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository
        extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    Optional<UserAccount> findByAgentId(String agentId);

    Optional<UserAccount> findByRole(AccountRole role);

    List<UserAccount> findAllByRoleAndAgentStatusOrderByCreatedAtAsc(
            AccountRole role,
            AgentStatus agentStatus
    );

    boolean existsByAgentId(String agentId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByContactNumber(String contactNumber);

    boolean existsByRole(AccountRole role);
}