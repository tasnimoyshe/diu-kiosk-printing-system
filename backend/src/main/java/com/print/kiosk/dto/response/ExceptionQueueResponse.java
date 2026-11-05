package com.print.kiosk.dto.response;
import com.print.kiosk.entity.*;
import java.time.LocalDateTime;
public record ExceptionQueueResponse(Long exceptionId, Long requestId, String trackingNumber, String fullName, String maskedRegistrationNumber, RequestExceptionType exceptionType, String reason, PrintRequestStatus status, String agentId, LocalDateTime createdAt) {}