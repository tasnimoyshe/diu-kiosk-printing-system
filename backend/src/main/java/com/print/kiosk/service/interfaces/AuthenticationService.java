package com.print.kiosk.service.interfaces;

import com.print.kiosk.dto.request.LoginRequest;
import com.print.kiosk.dto.response.CurrentUserResponse;
import com.print.kiosk.security.AuthenticationResult;

public interface AuthenticationService {

    AuthenticationResult login(
            LoginRequest request
    );

    AuthenticationResult refresh(
            String rawRefreshToken
    );

    void logout(
            String rawRefreshToken
    );

    CurrentUserResponse getCurrentUser(
            Long accountId
    );
}