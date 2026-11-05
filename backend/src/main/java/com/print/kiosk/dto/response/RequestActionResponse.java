package com.print.kiosk.dto.response;
import com.print.kiosk.entity.*;
import java.time.LocalDateTime;
public record RequestActionResponse(Long requestId, String trackingNumber, PrintRequestStatus status, PaymentStatus paymentStatus, Long assignedAgentAccountId, String assignedAgentId, LocalDateTime changedAt, String message) {}