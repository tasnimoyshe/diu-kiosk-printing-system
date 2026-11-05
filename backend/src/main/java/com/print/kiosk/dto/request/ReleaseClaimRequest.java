package com.print.kiosk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReleaseClaimRequest {
    @NotBlank @Size(max=255) private String reason;
    public String getReason(){return reason;}
    public void setReason(String value){this.reason=value;}
}