package com.print.kiosk.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieService {

    public static final String ACCESS_TOKEN_COOKIE =
            "kiosk_access_token";

    public static final String REFRESH_TOKEN_COOKIE =
            "kiosk_refresh_token";

    private static final String ACCESS_COOKIE_PATH = "/";
    private static final String REFRESH_COOKIE_PATH = "/api/auth";

    private final boolean secureCookies;

    public AuthCookieService(
            @Value("${app.security.cookie.secure:false}")
            boolean secureCookies
    ) {
        this.secureCookies = secureCookies;
    }

    public ResponseCookie createAccessTokenCookie(
            String accessToken,
            long maxAgeSeconds
    ) {
        return createCookie(
                ACCESS_TOKEN_COOKIE,
                accessToken,
                ACCESS_COOKIE_PATH,
                maxAgeSeconds
        );
    }

    public ResponseCookie createRefreshTokenCookie(
            String refreshToken,
            long maxAgeSeconds
    ) {
        return createCookie(
                REFRESH_TOKEN_COOKIE,
                refreshToken,
                REFRESH_COOKIE_PATH,
                maxAgeSeconds
        );
    }

    public ResponseCookie deleteAccessTokenCookie() {
        return deleteCookie(
                ACCESS_TOKEN_COOKIE,
                ACCESS_COOKIE_PATH
        );
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return deleteCookie(
                REFRESH_TOKEN_COOKIE,
                REFRESH_COOKIE_PATH
        );
    }

    private ResponseCookie createCookie(
            String name,
            String value,
            String path,
            long maxAgeSeconds
    ) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Lax")
                .path(path)
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }

    private ResponseCookie deleteCookie(
            String name,
            String path
    ) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Lax")
                .path(path)
                .maxAge(Duration.ZERO)
                .build();
    }
}