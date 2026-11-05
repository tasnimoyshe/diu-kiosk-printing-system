package com.print.kiosk.dto.request;

import com.print.kiosk.entity.RequestExceptionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReportProblemRequest {
    @NotNull private RequestExceptionType exceptionType;
    @NotBlank @Size(max=500) private String reason;
    public RequestExceptionType getExceptionType(){return exceptionType;} public void setExceptionType(RequestExceptionType v){exceptionType=v;}
    public String getReason(){return reason;} public void setReason(String v){reason=v;}
}