package com.print.kiosk.security;

import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.exception.AccountNotAllowedException;
import com.print.kiosk.repository.UserAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class AccountStatusFilter extends OncePerRequestFilter {

    private final UserAccountRepository userAccountRepository;
    private final AccountAccessPolicy accountAccessPolicy;
    private final SecurityErrorResponseWriter errorResponseWriter;

    public AccountStatusFilter(
            UserAccountRepository userAccountRepository,
            AccountAccessPolicy accountAccessPolicy,
            SecurityErrorResponseWriter errorResponseWriter
    ) {
        this.userAccountRepository = userAccountRepository;
        this.accountAccessPolicy = accountAccessPolicy;
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {

            filterChain.doFilter(request, response);
            return;
        }

        Long accountId = extractAccountId(jwt);

        if (accountId == null) {
            rejectAsUnauthorized(
                    request,
                    response,
                    "Access token does not identify a valid account"
            );
            return;
        }

        Optional<UserAccount> accountOptional =
                userAccountRepository.findById(accountId);

        if (accountOptional.isEmpty()) {
            rejectAsUnauthorized(
                    request,
                    response,
                    "Authenticated account no longer exists"
            );
            return;
        }

        UserAccount account = accountOptional.get();

        String tokenRole =
                jwt.getClaimAsString("role");

        if (tokenRole == null
                || !tokenRole.equals(account.getRole().name())) {

            rejectAsUnauthorized(
                    request,
                    response,
                    "Access token account information is no longer valid"
            );
            return;
        }

        try {
            accountAccessPolicy
                    .ensureAuthenticationAllowed(account);

        } catch (AccountNotAllowedException exception) {

            SecurityContextHolder.clearContext();

            errorResponseWriter.write(
                    request,
                    response,
                    HttpStatus.FORBIDDEN.value(),
                    HttpStatus.FORBIDDEN.getReasonPhrase(),
                    exception.getMessage()
            );

            return;
        }

        filterChain.doFilter(
                request,
                response
        );
    }

    private Long extractAccountId(
            Jwt jwt
    ) {
        Object claimValue =
                jwt.getClaim("accountId");

        if (claimValue instanceof Number number) {
            return number.longValue();
        }

        if (claimValue instanceof String text) {
            try {
                return Long.parseLong(text);

            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private void rejectAsUnauthorized(
            HttpServletRequest request,
            HttpServletResponse response,
            String message
    ) throws IOException {

        SecurityContextHolder.clearContext();

        errorResponseWriter.write(
                request,
                response,
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                message
        );
    }
}