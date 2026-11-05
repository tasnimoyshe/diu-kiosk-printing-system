package com.print.kiosk.dto.response;
import com.print.kiosk.entity.PaymentMethod;
public record PaymentOptionResponse(PaymentMethod method, boolean enabled, String label) {}