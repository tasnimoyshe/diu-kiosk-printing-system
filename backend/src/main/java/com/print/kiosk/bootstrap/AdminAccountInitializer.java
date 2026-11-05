package com.print.kiosk.bootstrap;

import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminFullName;
    private final String adminEmail;
    private final String adminPassword;

    public AdminAccountInitializer(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.full-name}") String adminFullName,
            @Value("${app.admin.email}") String adminEmail,
            @Value("${app.admin.password}") String adminPassword
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminFullName = adminFullName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userAccountRepository.existsByRole(AccountRole.ADMIN)) {
            LOGGER.info(
                    "Admin account already exists. Initialization skipped."
            );
            return;
        }

        validateAdminConfiguration();

        String normalizedEmail =
                adminEmail.trim().toLowerCase(Locale.ROOT);

        if (userAccountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalStateException(
                    "The configured admin email is already used "
                            + "by another account"
            );
        }

        UserAccount adminAccount = new UserAccount();
        adminAccount.setFullName(adminFullName.trim());
        adminAccount.setEmail(normalizedEmail);
        adminAccount.setPasswordHash(
                passwordEncoder.encode(adminPassword)
        );
        adminAccount.setRole(AccountRole.ADMIN);
        adminAccount.setAgentId(null);
        adminAccount.setContactNumber(null);
        adminAccount.setAgentStatus(null);

        userAccountRepository.saveAndFlush(adminAccount);

        LOGGER.info("Admin account created successfully.");
    }

    private void validateAdminConfiguration() {
        if (adminFullName == null || adminFullName.isBlank()) {
            throw new IllegalStateException(
                    "ADMIN_FULL_NAME environment variable is required"
            );
        }

        if (adminEmail == null || adminEmail.isBlank()) {
            throw new IllegalStateException(
                    "ADMIN_EMAIL environment variable is required"
            );
        }

        if (!adminEmail.trim().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        )) {
            throw new IllegalStateException(
                    "ADMIN_EMAIL must contain a valid email address"
            );
        }

        if (adminPassword == null
                || adminPassword.length() < 8
                || !adminPassword.matches(".*[A-Z].*")
                || !adminPassword.matches(".*[a-z].*")
                || !adminPassword.matches(".*\\d.*")) {
            throw new IllegalStateException(
                    "ADMIN_PASSWORD must contain at least 8 characters, "
                            + "one uppercase letter, one lowercase letter, "
                            + "and one digit"
            );
        }
    }
}