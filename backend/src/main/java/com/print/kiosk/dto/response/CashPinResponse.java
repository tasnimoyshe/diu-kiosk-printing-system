package com.print.kiosk.dto.response;
import java.time.LocalDateTime;
public record CashPinResponse(Long requestId, String trackingNumber, String pin, LocalDateTime expiresAt) {

}