package com.print.kiosk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @NotBlank(message = "Email address or agent ID is required")
    @Size(
            max = 150,
            message = "Email address or agent ID must not exceed 150 characters"
    )
    private String loginId;

    @NotBlank(message = "Password is required")
    @Size(
            max = 72,
            message = "Password must not exceed 72 characters"
    )
    private String password;

    public LoginRequest() {
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}