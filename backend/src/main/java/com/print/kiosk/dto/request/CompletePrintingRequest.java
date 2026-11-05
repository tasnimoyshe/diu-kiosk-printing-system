package com.print.kiosk.dto.request;

import jakarta.validation.constraints.AssertTrue;

public class CompletePrintingRequest {
    @AssertTrue(message="Completion must be explicitly confirmed") private boolean confirmed;
    public boolean isConfirmed(){return confirmed;} public void setConfirmed(boolean v){confirmed=v;}
}