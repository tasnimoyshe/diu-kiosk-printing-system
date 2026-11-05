package com.print.kiosk.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class AgentRegistrationRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Agent ID is required")
    @Size(max = 50, message = "Agent ID must not exceed 50 characters")
    private String agentId;

    @NotBlank(message = "Email address is required")
    @Email(message = "Email address is invalid")
    @Size(max = 150, message = "Email address must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Contact number is required")
    @Size(max = 30, message = "Contact number must not exceed 30 characters")
    private String contactNumber;

    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 72,
            message = "Password must contain between 8 and 72 characters"
    )
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, "
                    + "one lowercase letter, and one digit"
    )
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotNull(message = "ID card file is required")
    private MultipartFile idCard;

    public AgentRegistrationRequest() {
    }

    @AssertTrue(message = "Password and confirm password must match")
    public boolean isPasswordConfirmationValid() {
        return password != null && password.equals(confirmPassword);
    }

    @AssertTrue(message = "ID card file is required")
    public boolean isIdCardProvided() {
        return idCard != null && !idCard.isEmpty();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public MultipartFile getIdCard() {
        return idCard;
    }

    public void setIdCard(MultipartFile idCard) {
        this.idCard = idCard;
    }
}