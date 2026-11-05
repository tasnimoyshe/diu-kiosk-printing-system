package com.print.kiosk.security;

import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.AgentStatus;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.exception.AccountNotAllowedException;
import org.springframework.stereotype.Service;

@Service
public class AccountStatusService {

    public void validateAccessAllowed(UserAccount account) {
        if (account == null || account.getRole() == null) {
            throw new AccountNotAllowedException(
                    "This account is unavailable"
            );
        }

        if (account.getRole() == AccountRole.ADMIN) {
            return;
        }

        if (account.getRole() != AccountRole.AGENT) {
            throw new AccountNotAllowedException(
                    "This account is unavailable"
            );
        }

        AgentStatus status = account.getAgentStatus();

        if (status == null) {
            throw new AccountNotAllowedException(
                    "This agent account is unavailable"
            );
        }

        switch (status) {
            case APPROVED -> {
                return;
            }

            case PENDING -> throw new AccountNotAllowedException(
                    "This agent account is pending admin approval"
            );

            case REJECTED -> throw new AccountNotAllowedException(
                    "This agent account was rejected"
            );

            case SUSPENDED -> throw new AccountNotAllowedException(
                    "This agent account is suspended"
            );

            case DELETED -> throw new AccountNotAllowedException(
                    "This agent account is deleted"
            );
        }
    }
}