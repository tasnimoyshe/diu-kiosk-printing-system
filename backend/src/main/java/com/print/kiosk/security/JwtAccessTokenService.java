package com.print.kiosk.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JwtAccessTokenService {

    private static final String TOKEN_ISSUER = "kiosk-backend";
    private static final List<String> TOKEN_AUDIENCE =
            List.of("kiosk-web");

    private final JwtEncoder jwtEncoder;
    private final Duration accessTokenLifetime;

    public JwtAccessTokenService(
            JwtEncoder jwtEncoder,
            @Value("${app.security.jwt.access-token-minutes}")
            long accessTokenMinutes
    ) {
        if (accessTokenMinutes <= 0) {
            throw new IllegalStateException(
                    "Access-token duration must be greater than zero"
            );
        }

        this.jwtEncoder = jwtEncoder;
        this.accessTokenLifetime =
                Duration.ofMinutes(accessTokenMinutes);
    }

    public String generateAccessToken(
            AuthenticatedUserPrincipal principal
    ) {
        if (principal == null) {
            throw new IllegalArgumentException(
                    "Authenticated user principal is required"
            );
        }

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(accessTokenLifetime);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(TOKEN_ISSUER)
                .subject(principal.getEmail())
                .audience(TOKEN_AUDIENCE)
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim("accountId", principal.getAccountId())
                .claim("role", principal.getRole().name())
                .claim("tokenType", "access")
                .build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }

    public long getAccessTokenMaxAgeSeconds() {
        return accessTokenLifetime.toSeconds();
    }
}