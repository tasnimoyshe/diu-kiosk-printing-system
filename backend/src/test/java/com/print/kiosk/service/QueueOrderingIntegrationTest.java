package com.print.kiosk.service;

import com.print.kiosk.dto.response.QueueEntryResponse;
import com.print.kiosk.entity.PaymentStatus;
import com.print.kiosk.entity.PrintColor;
import com.print.kiosk.entity.PrintRequest;
import com.print.kiosk.entity.PrintRequestStatus;
import com.print.kiosk.entity.PrintingSide;
import com.print.kiosk.entity.UserType;
import com.print.kiosk.repository.CashPaymentPinRepository;
import com.print.kiosk.repository.FileDownloadEventRepository;
import com.print.kiosk.repository.PaymentRecordRepository;
import com.print.kiosk.repository.PrintRequestFileRepository;
import com.print.kiosk.repository.PrintRequestRepository;
import com.print.kiosk.repository.PrintRequestStatusHistoryRepository;
import com.print.kiosk.repository.RequestClaimRepository;
import com.print.kiosk.repository.RequestExceptionRepository;
import com.print.kiosk.service.interfaces.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QueueOrderingIntegrationTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private FileDownloadEventRepository fileDownloadEventRepository;

    @Autowired
    private CashPaymentPinRepository cashPaymentPinRepository;

    @Autowired
    private RequestExceptionRepository requestExceptionRepository;

    @Autowired
    private RequestClaimRepository requestClaimRepository;

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    @Autowired
    private PrintRequestStatusHistoryRepository
            printRequestStatusHistoryRepository;

    @Autowired
    private PrintRequestFileRepository printRequestFileRepository;

    @Autowired
    private PrintRequestRepository printRequestRepository;

    @BeforeEach
    void clearDatabase() {
        /*
         * Child records must be removed before parent print-request records.
         * The complete test transaction is rolled back after the test, ensuring
         * that queue test records cannot affect later integration tests.
         */
        fileDownloadEventRepository.deleteAllInBatch();
        cashPaymentPinRepository.deleteAllInBatch();
        requestExceptionRepository.deleteAllInBatch();
        requestClaimRepository.deleteAllInBatch();
        paymentRecordRepository.deleteAllInBatch();
        printRequestStatusHistoryRepository.deleteAllInBatch();
        printRequestFileRepository.deleteAllInBatch();
        printRequestRepository.deleteAllInBatch();
    }

    @Test
    void paidRequestsComeBeforeUnpaidAndKeepSubmissionOrder() {
        PrintRequest unpaidRequest = saveRequest(
                "PR-U",
                PaymentStatus.PENDING,
                PrintRequestStatus.AWAITING_PAYMENT
        );

        PrintRequest firstPaidRequest = saveRequest(
                "PR-P1",
                PaymentStatus.PAID,
                PrintRequestStatus.PAID
        );

        PrintRequest secondPaidRequest = saveRequest(
                "PR-P2",
                PaymentStatus.PAID,
                PrintRequestStatus.PAID
        );

        List<QueueEntryResponse> queueEntries =
                queueService
                        .getPublicQueue(
                                null,
                                PageRequest.of(0, 20)
                        )
                        .getContent();

        assertEquals(
                List.of(
                        firstPaidRequest.getId(),
                        secondPaidRequest.getId(),
                        unpaidRequest.getId()
                ),
                queueEntries.stream()
                        .map(QueueEntryResponse::requestId)
                        .toList()
        );

        assertEquals(
                List.of(1L, 2L, 3L),
                queueEntries.stream()
                        .map(QueueEntryResponse::queuePosition)
                        .toList()
        );
    }

    private PrintRequest saveRequest(
            String trackingNumber,
            PaymentStatus paymentStatus,
            PrintRequestStatus requestStatus
    ) {
        PrintRequest request = new PrintRequest();

        request.setTrackingNumber(trackingNumber);
        request.setFullName("Queue User");
        request.setRegistrationNumber("242-15-125");
        request.setUserType(UserType.STUDENT);
        request.setPrintColor(PrintColor.COLOR);
        request.setPrintingSide(PrintingSide.SINGLE_SIDED);
        request.setCopies(1);
        request.setStatus(requestStatus);
        request.setPaymentStatus(paymentStatus);
        request.setPricingConfigId(1L);
        request.setPricingRevision(1);
        request.setAppliedRate(new BigDecimal("10.00"));
        request.setTotalDocumentPages(1);
        request.setTotalPhysicalSheets(1);
        request.setTotalAmount(new BigDecimal("10.00"));
        request.setCalculatedAt(LocalDateTime.now());
        request.setExpiresAt(LocalDateTime.now().plusHours(12));

        return printRequestRepository.saveAndFlush(request);
    }
}