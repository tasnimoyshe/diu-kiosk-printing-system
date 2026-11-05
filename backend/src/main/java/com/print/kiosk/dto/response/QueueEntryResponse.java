package com.print.kiosk.dto.response;

import com.print.kiosk.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QueueEntryResponse(
        long queuePosition,
        Long requestId,
        String trackingNumber,
        String fullName,
        String maskedRegistrationNumber,
        UserType userType,
        PaymentStatus paymentStatus,
        PrintRequestStatus requestStatus,
        BigDecimal totalAmount,
        Long assignedAgentAccountId,
        String assignedAgentId,
        LocalDateTime submittedAt,
        LocalDateTime claimedAt
) {}