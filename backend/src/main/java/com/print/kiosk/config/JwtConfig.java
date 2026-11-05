package com.print.kiosk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
public class JwtConfig {

    private static final String TOKEN_ISSUER =
            "kiosk-backend";

    private static final String TOKEN_AUDIENCE =
            "kiosk-web";

    private final String encodedJwtSecret;

    public JwtConfig(
            @Value("${app.security.jwt.secret}")
            String encodedJwtSecret
    ) {
        this.encodedJwtSecret = encodedJwtSecret;
    }

    @Bean
    public SecretKey jwtSecretKey() {
        byte[] decodedSecret;

        try {
            decodedSecret = Base64.getDecoder()
                    .decode(encodedJwtSecret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "JWT_SECRET must contain a valid Base64 value",
                    exception
            );
        }

        if (decodedSecret.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must contain at least 32 decoded bytes"
            );
        }

        return new SecretKeySpec(
                decodedSecret,
                "HmacSHA256"
        );
    }

    @Bean
    public JwtEncoder jwtEncoder(
            SecretKey jwtSecretKey
    ) {
        return NimbusJwtEncoder
                .withSecretKey(jwtSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        OAuth2TokenValidator<Jwt> defaultValidator =
                JwtValidators.createDefaultWithIssuer(
                        TOKEN_ISSUER
                );

        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtClaimValidator<List<String>>(
                        "aud",
                        audience -> audience != null
                                && audience.contains(
                                TOKEN_AUDIENCE
                        )
                );

        OAuth2TokenValidator<Jwt> tokenTypeValidator =
                new JwtClaimValidator<String>(
                        "tokenType",
                        "access"::equals
                );

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        defaultValidator,
                        audienceValidator,
                        tokenTypeValidator
                )
        );

        return decoder;
    }
}