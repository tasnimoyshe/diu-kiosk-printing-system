package com.print.kiosk.dto.response;

import com.print.kiosk.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProcessingRequestResponse(
        Long requestId, String trackingNumber, String fullName, String registrationNumber,
        UserType userType, String department, PaymentStatus paymentStatus, PrintRequestStatus requestStatus,
        PrintColor printColor, PrintingSide printingSide, int copies, long totalDocumentPages,
        long totalPhysicalSheets, BigDecimal appliedRate, BigDecimal totalAmount,
        Long assignedAgentAccountId, String assignedAgentId, LocalDateTime claimedAt,
        LocalDateTime printingStartedAt, LocalDateTime completedAt, List<ProcessingFileResponse> files
) { public ProcessingRequestResponse { files = List.copyOf(files); } }