package com.print.kiosk.service.impl;

import com.print.kiosk.entity.RefreshToken;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.exception.AccountNotAllowedException;
import com.print.kiosk.exception.InvalidRefreshTokenException;
import com.print.kiosk.repository.RefreshTokenRepository;
import com.print.kiosk.security.AccountStatusService;
import com.print.kiosk.security.RefreshTokenSession;
import com.print.kiosk.service.interfaces.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenServiceImpl
        implements RefreshTokenService {

    private static final int TOKEN_BYTES = 48;

    private static final int MAX_GENERATION_ATTEMPTS =
            5;

    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountStatusService accountStatusService;
    private final SecureRandom secureRandom;
    private final Duration refreshTokenLifetime;

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            AccountStatusService accountStatusService,
            @Value("${app.security.jwt.refresh-token-days}")
            long refreshTokenDays
    ) {
        if (refreshTokenDays <= 0) {
            throw new IllegalStateException(
                    "Refresh-token duration must be greater than zero"
            );
        }

        this.refreshTokenRepository =
                refreshTokenRepository;

        this.accountStatusService =
                accountStatusService;

        this.secureRandom =
                new SecureRandom();

        this.refreshTokenLifetime =
                Duration.ofDays(refreshTokenDays);
    }

    @Override
    @Transactional
    public RefreshTokenSession create(
            UserAccount userAccount
    ) {
        if (userAccount == null
                || userAccount.getId() == null) {

            throw new IllegalArgumentException(
                    "A persisted user account is required"
            );
        }

        accountStatusService.validateAccessAllowed(
                userAccount
        );

        return createPersistedSession(
                userAccount,
                Instant.now()
        );
    }

    @Override
    @Transactional(
            noRollbackFor = {
                    InvalidRefreshTokenException.class,
                    AccountNotAllowedException.class
            }
    )
    public RefreshTokenSession rotate(
            String rawRefreshToken
    ) {
        String tokenHash = hashToken(
                requireToken(rawRefreshToken)
        );

        Instant now = Instant.now();

        RefreshToken currentToken =
                refreshTokenRepository
                        .findByTokenHashForUpdate(
                                tokenHash
                        )
                        .orElseThrow(
                                () -> new InvalidRefreshTokenException(
                                        "Refresh token is invalid"
                                )
                        );

        if (currentToken.isRevoked()) {
            refreshTokenRepository
                    .revokeAllActiveByAccountId(
                            currentToken
                                    .getUserAccount()
                                    .getId(),
                            now
                    );

            throw new InvalidRefreshTokenException(
                    "Refresh token has already been used or revoked"
            );
        }

        if (!currentToken
                .getExpiresAt()
                .isAfter(now)) {

            currentToken.setLastUsedAt(now);
            currentToken.setRevokedAt(now);

            refreshTokenRepository.saveAndFlush(
                    currentToken
            );

            throw new InvalidRefreshTokenException(
                    "Refresh token has expired"
            );
        }

        UserAccount userAccount =
                currentToken.getUserAccount();

        try {
            accountStatusService.validateAccessAllowed(
                    userAccount
            );
        } catch (AccountNotAllowedException exception) {
            refreshTokenRepository
                    .revokeAllActiveByAccountId(
                            userAccount.getId(),
                            now
                    );

            throw exception;
        }

        currentToken.setLastUsedAt(now);
        currentToken.setRevokedAt(now);

        refreshTokenRepository.saveAndFlush(
                currentToken
        );

        return createPersistedSession(
                userAccount,
                now
        );
    }

    @Override
    @Transactional
    public void revoke(
            String rawRefreshToken
    ) {
        if (rawRefreshToken == null
                || rawRefreshToken.isBlank()) {

            return;
        }

        String tokenHash =
                hashToken(rawRefreshToken);

        Instant now = Instant.now();

        refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .ifPresent(refreshToken -> {
                    if (!refreshToken.isRevoked()) {
                        refreshToken.setLastUsedAt(now);
                        refreshToken.setRevokedAt(now);

                        refreshTokenRepository
                                .saveAndFlush(
                                        refreshToken
                                );
                    }
                });
    }

    @Override
    @Transactional
    public void revokeAllForAccount(
            Long accountId
    ) {
        if (accountId == null) {
            return;
        }

        refreshTokenRepository
                .revokeAllActiveByAccountId(
                        accountId,
                        Instant.now()
                );
    }

    private RefreshTokenSession createPersistedSession(
            UserAccount userAccount,
            Instant now
    ) {
        for (
                int attempt = 1;
                attempt <= MAX_GENERATION_ATTEMPTS;
                attempt++
        ) {
            String rawToken =
                    generateRawToken();

            String tokenHash =
                    hashToken(rawToken);

            if (refreshTokenRepository
                    .existsByTokenHash(tokenHash)) {

                continue;
            }

            RefreshToken refreshToken =
                    new RefreshToken();

            refreshToken.setUserAccount(
                    userAccount
            );

            refreshToken.setTokenHash(
                    tokenHash
            );

            refreshToken.setExpiresAt(
                    now.plus(refreshTokenLifetime)
            );

            refreshTokenRepository.saveAndFlush(
                    refreshToken
            );

            return new RefreshTokenSession(
                    rawToken,
                    userAccount,
                    refreshTokenLifetime.toSeconds()
            );
        }

        throw new IllegalStateException(
                "Unable to generate a unique refresh token"
        );
    }

    private String generateRawToken() {
        byte[] tokenBytes =
                new byte[TOKEN_BYTES];

        secureRandom.nextBytes(tokenBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }

    private String requireToken(
            String rawRefreshToken
    ) {
        if (rawRefreshToken == null
                || rawRefreshToken.isBlank()) {

            throw new InvalidRefreshTokenException(
                    "Refresh token cookie is missing"
            );
        }

        return rawRefreshToken;
    }

    private String hashToken(
            String rawToken
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash = digest.digest(
                    rawToken.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

            return HexFormat.of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }
}