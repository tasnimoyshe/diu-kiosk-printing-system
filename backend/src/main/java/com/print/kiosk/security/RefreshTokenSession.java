package com.print.kiosk.security;

import com.print.kiosk.entity.UserAccount;

public record RefreshTokenSession(
        String rawToken,
        UserAccount userAccount,
        long maxAgeSeconds
) {
}