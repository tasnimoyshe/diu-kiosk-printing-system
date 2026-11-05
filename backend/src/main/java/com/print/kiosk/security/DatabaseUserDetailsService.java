package com.print.kiosk.security;

import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public DatabaseUserDetailsService(
            UserAccountRepository userAccountRepository
    ) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId)
            throws UsernameNotFoundException {

        if (loginId == null || loginId.isBlank()) {
            throw new UsernameNotFoundException(
                    "Invalid email address, agent ID, or password"
            );
        }

        String normalizedLoginId = loginId.trim();

        Optional<UserAccount> accountByEmail =
                userAccountRepository.findByEmailIgnoreCase(
                        normalizedLoginId
                );

        UserAccount account = accountByEmail.orElseGet(
                () -> userAccountRepository
                        .findByAgentId(
                                normalizedLoginId.toUpperCase(
                                        Locale.ROOT
                                )
                        )
                        .orElseThrow(
                                () -> new UsernameNotFoundException(
                                        "Invalid email address, "
                                                + "agent ID, or password"
                                )
                        )
        );

        return new AuthenticatedUserPrincipal(
                account.getId(),
                account.getFullName(),
                account.getAgentId(),
                account.getEmail(),
                account.getPasswordHash(),
                account.getRole(),
                account.getAgentStatus()
        );
    }
}