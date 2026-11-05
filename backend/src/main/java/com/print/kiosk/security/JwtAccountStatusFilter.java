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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtAccountStatusFilter extends OncePerRequestFilter {

    private final UserAccountRepository userAccountRepository;
    private final AccountStatusService accountStatusService;
    private final SecurityErrorResponseWriter errorResponseWriter;

    public JwtAccountStatusFilter(
            UserAccountRepository userAccountRepository,
            AccountStatusService accountStatusService,
            SecurityErrorResponseWriter errorResponseWriter
    ) {
        this.userAccountRepository = userAccountRepository;
        this.accountStatusService = accountStatusService;
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (!(authentication
                instanceof JwtAuthenticationToken jwtAuthentication)
                || !authentication.isAuthenticated()) {

            filterChain.doFilter(request, response);
            return;
        }

        Number accountIdClaim = jwtAuthentication
                .getToken()
                .getClaim("accountId");

        if (accountIdClaim == null) {
            rejectUnauthorized(request, response);
            return;
        }

        Optional<UserAccount> accountOptional =
                userAccountRepository.findById(
                        accountIdClaim.longValue()
                );

        if (accountOptional.isEmpty()) {
            rejectUnauthorized(request, response);
            return;
        }

        UserAccount account = accountOptional.get();

        String tokenRole = jwtAuthentication
                .getToken()
                .getClaimAsString("role");

        if (tokenRole == null
                || account.getRole() == null
                || !account.getRole().name().equals(tokenRole)) {

            rejectUnauthorized(request, response);
            return;
        }

        try {
            accountStatusService.validateAccessAllowed(account);
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

        filterChain.doFilter(request, response);
    }

    private void rejectUnauthorized(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        SecurityContextHolder.clearContext();

        errorResponseWriter.write(
                request,
                response,
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "The access token does not reference a valid account"
        );
    }
}