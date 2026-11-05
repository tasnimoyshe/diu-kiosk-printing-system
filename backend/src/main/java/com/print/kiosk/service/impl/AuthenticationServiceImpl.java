package com.print.kiosk.service.impl;

import com.print.kiosk.dto.request.LoginRequest;
import com.print.kiosk.dto.response.CurrentUserResponse;
import com.print.kiosk.dto.response.LoginResponse;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.exception.AccountNotAllowedException;
import com.print.kiosk.exception.InvalidCredentialsException;
import com.print.kiosk.exception.InvalidRefreshTokenException;
import com.print.kiosk.repository.UserAccountRepository;
import com.print.kiosk.security.AccountStatusService;
import com.print.kiosk.security.AuthenticatedUserPrincipal;
import com.print.kiosk.security.AuthenticationResult;
import com.print.kiosk.security.JwtAccessTokenService;
import com.print.kiosk.security.RefreshTokenSession;
import com.print.kiosk.service.interfaces.AuthenticationService;
import com.print.kiosk.service.interfaces.RefreshTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class AuthenticationServiceImpl
        implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtAccessTokenService jwtAccessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserAccountRepository userAccountRepository;
    private final AccountStatusService accountStatusService;

    public AuthenticationServiceImpl(
            AuthenticationManager authenticationManager,
            JwtAccessTokenService jwtAccessTokenService,
            RefreshTokenService refreshTokenService,
            UserAccountRepository userAccountRepository,
            AccountStatusService accountStatusService
    ) {
        this.authenticationManager =
                authenticationManager;

        this.jwtAccessTokenService =
                jwtAccessTokenService;

        this.refreshTokenService =
                refreshTokenService;

        this.userAccountRepository =
                userAccountRepository;

        this.accountStatusService =
                accountStatusService;
    }

    @Override
    public AuthenticationResult login(
            LoginRequest request
    ) {
        String normalizedLoginId =
                request.getLoginId().trim();

        Optional<UserAccount> knownAccount =
                findAccountByLoginId(
                        normalizedLoginId
                );

        knownAccount.ifPresent(
                accountStatusService::validateAccessAllowed
        );

        Authentication authentication;

        try {
            authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    normalizedLoginId,
                                    request.getPassword()
                            )
                    );

        } catch (DisabledException exception) {
            knownAccount.ifPresent(
                    accountStatusService::validateAccessAllowed
            );

            throw new AccountNotAllowedException(
                    "This account is not approved or is unavailable"
            );

        } catch (
                BadCredentialsException
                | UsernameNotFoundException exception
        ) {
            throw new InvalidCredentialsException(
                    "Invalid email address, agent ID, or password"
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal
                instanceof AuthenticatedUserPrincipal authenticatedUser)) {

            throw new IllegalStateException(
                    "Authentication completed without a valid account principal"
            );
        }

        UserAccount account =
                userAccountRepository
                        .findById(
                                authenticatedUser
                                        .getAccountId()
                        )
                        .orElseThrow(
                                () -> new InvalidCredentialsException(
                                        "Invalid email address, agent ID, or password"
                                )
                        );

        accountStatusService.validateAccessAllowed(
                account
        );

        RefreshTokenSession refreshSession =
                refreshTokenService.create(
                        account
                );

        return createAuthenticationResult(
                account,
                refreshSession,
                "Login successful"
        );
    }

    @Override
    public AuthenticationResult refresh(
            String rawRefreshToken
    ) {
        if (rawRefreshToken == null
                || rawRefreshToken.isBlank()) {

            throw new InvalidRefreshTokenException(
                    "Refresh token cookie is missing"
            );
        }

        RefreshTokenSession refreshSession =
                refreshTokenService.rotate(
                        rawRefreshToken
                );

        return createAuthenticationResult(
                refreshSession.userAccount(),
                refreshSession,
                "Session refreshed successfully"
        );
    }

    @Override
    public void logout(
            String rawRefreshToken
    ) {
        refreshTokenService.revoke(
                rawRefreshToken
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(
            Long accountId
    ) {
        if (accountId == null) {
            throw new InvalidCredentialsException(
                    "Authenticated account information is missing"
            );
        }

        UserAccount account =
                userAccountRepository
                        .findById(accountId)
                        .orElseThrow(
                                () -> new InvalidCredentialsException(
                                        "Authenticated account was not found"
                                )
                        );

        accountStatusService.validateAccessAllowed(
                account
        );

        return new CurrentUserResponse(
                account.getId(),
                account.getFullName(),
                account.getAgentId(),
                account.getEmail(),
                account.getRole(),
                account.getAgentStatus()
        );
    }

    private AuthenticationResult createAuthenticationResult(
            UserAccount account,
            RefreshTokenSession refreshSession,
            String message
    ) {
        AuthenticatedUserPrincipal principal =
                new AuthenticatedUserPrincipal(
                        account.getId(),
                        account.getFullName(),
                        account.getAgentId(),
                        account.getEmail(),
                        account.getPasswordHash(),
                        account.getRole(),
                        account.getAgentStatus()
                );

        String accessToken =
                jwtAccessTokenService
                        .generateAccessToken(
                                principal
                        );

        LoginResponse loginResponse =
                new LoginResponse(
                        account.getId(),
                        account.getFullName(),
                        account.getAgentId(),
                        account.getEmail(),
                        account.getRole(),
                        account.getAgentStatus(),
                        message
                );

        return new AuthenticationResult(
                loginResponse,
                accessToken,
                jwtAccessTokenService
                        .getAccessTokenMaxAgeSeconds(),
                refreshSession.rawToken(),
                refreshSession.maxAgeSeconds()
        );
    }

    private Optional<UserAccount> findAccountByLoginId(
            String loginId
    ) {
        Optional<UserAccount> accountByEmail =
                userAccountRepository
                        .findByEmailIgnoreCase(
                                loginId
                        );

        if (accountByEmail.isPresent()) {
            return accountByEmail;
        }

        return userAccountRepository
                .findByAgentId(
                        loginId.toUpperCase(
                                Locale.ROOT
                        )
                );
    }
}