package com.print.kiosk.service.interfaces;

import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.security.RefreshTokenSession;

public interface RefreshTokenService {

    RefreshTokenSession create(
            UserAccount userAccount
    );

    RefreshTokenSession rotate(
            String rawRefreshToken
    );

    void revoke(
            String rawRefreshToken
    );

    void revokeAllForAccount(
            Long accountId
    );
}