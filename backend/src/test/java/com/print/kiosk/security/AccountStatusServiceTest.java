package com.print.kiosk.security;

import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.AgentStatus;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.exception.AccountNotAllowedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountStatusServiceTest {

    private final AccountStatusService accountStatusService =
            new AccountStatusService();

    @Test
    void adminIsAllowed() {
        UserAccount account =
                account(
                        AccountRole.ADMIN,
                        null
                );

        assertDoesNotThrow(
                () -> accountStatusService
                        .validateAccessAllowed(
                                account
                        )
        );
    }

    @Test
    void approvedAgentIsAllowed() {
        UserAccount account =
                account(
                        AccountRole.AGENT,
                        AgentStatus.APPROVED
                );

        assertDoesNotThrow(
                () -> accountStatusService
                        .validateAccessAllowed(
                                account
                        )
        );
    }

    @Test
    void pendingAgentIsBlocked() {
        assertStatusBlocked(
                AgentStatus.PENDING,
                "pending"
        );
    }

    @Test
    void rejectedAgentIsBlocked() {
        assertStatusBlocked(
                AgentStatus.REJECTED,
                "rejected"
        );
    }

    @Test
    void suspendedAgentIsBlocked() {
        assertStatusBlocked(
                AgentStatus.SUSPENDED,
                "suspended"
        );
    }

    @Test
    void deletedAgentIsBlocked() {
        assertStatusBlocked(
                AgentStatus.DELETED,
                "deleted"
        );
    }

    private void assertStatusBlocked(
            AgentStatus status,
            String expectedMessagePart
    ) {
        UserAccount account =
                account(
                        AccountRole.AGENT,
                        status
                );

        AccountNotAllowedException exception =
                assertThrows(
                        AccountNotAllowedException.class,
                        () -> accountStatusService
                                .validateAccessAllowed(
                                        account
                                )
                );

        assertTrue(
                exception.getMessage()
                        .contains(
                                expectedMessagePart
                        )
        );
    }

    private UserAccount account(
            AccountRole role,
            AgentStatus status
    ) {
        UserAccount account =
                new UserAccount();

        account.setRole(role);
        account.setAgentStatus(status);

        return account;
    }
}