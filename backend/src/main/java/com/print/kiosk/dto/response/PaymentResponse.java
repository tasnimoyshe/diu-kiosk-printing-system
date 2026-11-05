package com.print.kiosk.dto.response;
import com.print.kiosk.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record PaymentResponse(Long paymentId, Long requestId, String trackingNumber, PaymentMethod method, PaymentStatus paymentStatus, PrintRequestStatus requestStatus, BigDecimal amount, LocalDateTime completedAt, String message) {}