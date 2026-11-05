package com.print.kiosk.security;

import com.print.kiosk.dto.response.LoginResponse;

public record AuthenticationResult(
        LoginResponse response,
        String accessToken,
        long accessTokenMaxAgeSeconds,
        String refreshToken,
        long refreshTokenMaxAgeSeconds
) {
}