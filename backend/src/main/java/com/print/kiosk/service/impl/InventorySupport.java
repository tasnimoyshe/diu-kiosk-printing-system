package com.print.kiosk.service.impl;

import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.AgentStatus;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.exception.UnauthorizedRequestOperationException;
import com.print.kiosk.exception.ResourceNotFoundException;
import com.print.kiosk.repository.UserAccountRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Component
public class InventorySupport {

    private final UserAccountRepository userAccountRepository;

    public InventorySupport(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public UserAccount approvedAgent(Long accountId) {
        UserAccount account = userAccountRepository
                .findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Agent account was not found"
                ));

        if (account.getRole() != AccountRole.AGENT
                || account.getAgentStatus() != AgentStatus.APPROVED) {
            throw new UnauthorizedRequestOperationException(
                    "Only an approved agent may manage inventory"
            );
        }

        return account;
    }

    public String cleanName(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        return value.trim().replaceAll("\\s+", " ");
    }

    public String normalizeName(String value) {
        return cleanName(value, "Name").toLowerCase(Locale.ROOT);
    }

    public BigDecimal money(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Money value is required");
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Money value cannot be negative");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}