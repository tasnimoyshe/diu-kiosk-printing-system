package com.print.kiosk.service.impl;

import com.print.kiosk.entity.PrintRequest;
import com.print.kiosk.entity.PrintRequestStatus;
import com.print.kiosk.entity.PrintRequestStatusHistory;
import com.print.kiosk.entity.StatusActorType;
import com.print.kiosk.repository.PrintRequestFileRepository;
import com.print.kiosk.repository.PrintRequestRepository;
import com.print.kiosk.repository.PrintRequestStatusHistoryRepository;
import com.print.kiosk.service.interfaces.PrintRequestExpirationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class PrintRequestExpirationServiceImpl implements PrintRequestExpirationService {

    private static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");

    private final PrintRequestRepository printRequestRepository;
    private final PrintRequestFileRepository printRequestFileRepository;
    private final PrintRequestStatusHistoryRepository statusHistoryRepository;
    private final long expirationHours;
    private final long fileDeletionDelayHours;

    public PrintRequestExpirationServiceImpl(
            PrintRequestRepository printRequestRepository,
            PrintRequestFileRepository printRequestFileRepository,
            PrintRequestStatusHistoryRepository statusHistoryRepository,
            @Value("${app.print-request.expiration-hours:12}") long expirationHours,
            @Value("${app.print-request.file-deletion-delay-hours:24}") long fileDeletionDelayHours
    ) {
        this.printRequestRepository = printRequestRepository;
        this.printRequestFileRepository = printRequestFileRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.expirationHours = expirationHours;
        this.fileDeletionDelayHours = fileDeletionDelayHours;
    }

    @Override
    @Transactional
    public int expireDueRequests() {
        LocalDateTime now = LocalDateTime.now(DHAKA_ZONE);
        List<PrintRequest> dueRequests = printRequestRepository
                .findTop100ByStatusAndExpiresAtLessThanEqualOrderByExpiresAtAsc(
                        PrintRequestStatus.AWAITING_PAYMENT,
                        now
                );

        for (PrintRequest request : dueRequests) {
            request.setStatus(PrintRequestStatus.EXPIRED);
            printRequestRepository.save(request);

            PrintRequestStatusHistory history = new PrintRequestStatusHistory();
            history.setPrintRequest(request);
            history.setPreviousStatus(PrintRequestStatus.AWAITING_PAYMENT);
            history.setNewStatus(PrintRequestStatus.EXPIRED);
            history.setActorType(StatusActorType.SYSTEM);
            history.setReason("Unpaid request expired after " + expirationHours + " hours");
            statusHistoryRepository.save(history);

            printRequestFileRepository.markDeletionEligibleByRequestId(
                    request.getId(),
                    now.plusHours(fileDeletionDelayHours)
            );
        }

        return dueRequests.size();
    }
}