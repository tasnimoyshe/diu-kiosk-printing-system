package com.print.kiosk.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

@Component
public class CookieBearerTokenResolver implements BearerTokenResolver {

    private static final String ACCESS_TOKEN_COOKIE =
            "kiosk_access_token";

    @Override
    public String resolve(HttpServletRequest request) {
        if (isPublicEndpoint(request.getRequestURI())) {
            return null;
        }

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())
                    && cookie.getValue() != null
                    && !cookie.getValue().isBlank()) {

                return cookie.getValue();
            }
        }

        return null;
    }

    private boolean isPublicEndpoint(String requestUri) {
        return requestUri.equals("/api/test")
                || requestUri.startsWith("/api/print-requests")
                || requestUri.equals("/api/auth/login")
                || requestUri.equals("/api/auth/csrf")
                || requestUri.equals("/api/auth/agents/register");
    }
}