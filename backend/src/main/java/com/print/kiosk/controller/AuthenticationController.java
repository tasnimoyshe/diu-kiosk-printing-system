package com.print.kiosk.controller;

import com.print.kiosk.dto.request.LoginRequest;
import com.print.kiosk.dto.response.CurrentUserResponse;
import com.print.kiosk.dto.response.LoginResponse;
import com.print.kiosk.security.AuthCookieService;
import com.print.kiosk.security.AuthenticationResult;
import com.print.kiosk.service.interfaces.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final AuthCookieService authCookieService;

    public AuthenticationController(
            AuthenticationService authenticationService,
            AuthCookieService authCookieService
    ) {
        this.authenticationService =
                authenticationService;

        this.authCookieService =
                authCookieService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthenticationResult result =
                authenticationService.login(
                        request
                );

        return buildSessionResponse(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(
                    name = AuthCookieService.REFRESH_TOKEN_COOKIE,
                    required = false
            )
            String rawRefreshToken
    ) {
        AuthenticationResult result =
                authenticationService.refresh(
                        rawRefreshToken
                );

        return buildSessionResponse(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(
                    name = AuthCookieService.REFRESH_TOKEN_COOKIE,
                    required = false
            )
            String rawRefreshToken
    ) {
        authenticationService.logout(
                rawRefreshToken
        );

        return ResponseEntity
                .noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        authCookieService
                                .deleteAccessTokenCookie()
                                .toString()
                )
                .header(
                        HttpHeaders.SET_COOKIE,
                        authCookieService
                                .deleteRefreshTokenCookie()
                                .toString()
                )
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse>
    getCurrentUser(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Number accountId =
                jwt.getClaim("accountId");

        CurrentUserResponse response =
                authenticationService
                        .getCurrentUser(
                                accountId == null
                                        ? null
                                        : accountId.longValue()
                        );

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<LoginResponse>
    buildSessionResponse(
            AuthenticationResult result
    ) {
        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        authCookieService
                                .createAccessTokenCookie(
                                        result.accessToken(),
                                        result.accessTokenMaxAgeSeconds()
                                )
                                .toString()
                )
                .header(
                        HttpHeaders.SET_COOKIE,
                        authCookieService
                                .createRefreshTokenCookie(
                                        result.refreshToken(),
                                        result.refreshTokenMaxAgeSeconds()
                                )
                                .toString()
                )
                .body(result.response());
    }
}