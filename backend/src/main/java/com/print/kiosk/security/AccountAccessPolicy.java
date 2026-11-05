package com.print.kiosk.security;

import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.AgentStatus;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.exception.AccountNotAllowedException;
import org.springframework.stereotype.Component;

@Component
public class AccountAccessPolicy {

    public void ensureAuthenticationAllowed(UserAccount account) {
        if (account == null) {
            throw new AccountNotAllowedException(
                    "This account is unavailable"
            );
        }

        ensureAuthenticationAllowed(
                account.getRole(),
                account.getAgentStatus()
        );
    }

    public void ensureAuthenticationAllowed(
            AccountRole role,
            AgentStatus agentStatus
    ) {
        if (role == AccountRole.ADMIN) {
            return;
        }

        if (role != AccountRole.AGENT) {
            throw new AccountNotAllowedException(
                    "This account is unavailable"
            );
        }

        if (agentStatus == null) {
            throw new AccountNotAllowedException(
                    "This agent account is unavailable"
            );
        }

        switch (agentStatus) {
            case APPROVED -> {
                return;
            }

            case PENDING -> throw new AccountNotAllowedException(
                    "This agent account is waiting for admin approval"
            );

            case REJECTED -> throw new AccountNotAllowedException(
                    "This agent account was rejected"
            );

            case SUSPENDED -> throw new AccountNotAllowedException(
                    "This agent account is suspended"
            );

            case DELETED -> throw new AccountNotAllowedException(
                    "This agent account is unavailable"
            );
        }
    }
}