package com.print.kiosk.service;

import com.print.kiosk.dto.request.*;
import com.print.kiosk.entity.*;
import com.print.kiosk.exception.InvalidQueueActionException;
import com.print.kiosk.repository.*;
import com.print.kiosk.service.interfaces.RequestProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RequestProcessingServiceIntegrationTest {
    @Autowired private RequestProcessingService service;
    @Autowired private UserAccountRepository accounts;
    @Autowired private PrintRequestRepository requests;
    @Autowired private PrintRequestFileRepository files;
    @Autowired private RequestClaimRepository claims;
    @Autowired private RequestExceptionRepository exceptions;
    @Autowired private FileDownloadEventRepository downloads;

    @Test
    void claimNextClaimsOldestPaidRequest() {
        UserAccount agent = agent("AGT-CLAIM-1", "claim1@test.local");
        PrintRequest first = paid("PR-CLAIM-1");
        paid("PR-CLAIM-2");
        service.claimNext(agent.getId());
        PrintRequest saved = requests.findById(first.getId()).orElseThrow();
        assertEquals(PrintRequestStatus.CLAIMED, saved.getStatus());
        assertEquals(agent.getId(), saved.getAssignedAgent().getId());
        assertTrue(claims.existsByAgent_IdAndActiveTrue(agent.getId()));
    }

    @Test
    void agentCannotHoldTwoActiveClaims() {
        UserAccount agent = agent("AGT-CLAIM-2", "claim2@test.local");
        paid("PR-CLAIM-3");
        paid("PR-CLAIM-4");
        service.claimNext(agent.getId());
        assertThrows(InvalidQueueActionException.class, () -> service.claimNext(agent.getId()));
    }

    @Test
    void downloadStartsDownloadingAndRecordsEvent() {
        UserAccount agent = agent("AGT-CLAIM-3", "claim3@test.local");
        PrintRequest request = paid("PR-CLAIM-5");
        PrintRequestFile file = file(request);
        service.claimNext(agent.getId());
        service.downloadFile(request.getId(), file.getId(), agent.getId(), "127.0.0.1");
        assertEquals(PrintRequestStatus.DOWNLOADING,
                requests.findById(request.getId()).orElseThrow().getStatus());
        assertEquals(1, downloads.countByPrintRequest_Id(request.getId()));
    }

    @Test
    void printingCanCompleteAndMarksFilesForDeletion() {
        UserAccount agent = agent("AGT-CLAIM-4", "claim4@test.local");
        PrintRequest request = paid("PR-CLAIM-6");
        PrintRequestFile file = file(request);
        service.claimNext(agent.getId());
        service.downloadFile(request.getId(), file.getId(), agent.getId(), "127.0.0.1");
        service.startPrinting(request.getId(), agent.getId());
        CompletePrintingRequest complete = new CompletePrintingRequest();
        complete.setConfirmed(true);
        service.complete(request.getId(), agent.getId(), complete);
        PrintRequest saved = requests.findById(request.getId()).orElseThrow();
        assertEquals(PrintRequestStatus.COMPLETED, saved.getStatus());
        assertNotNull(saved.getCompletedAt());
        assertNotNull(files.findById(file.getId()).orElseThrow().getDeletionEligibleAt());
    }

    @Test
    void reportProblemMovesRequestToExceptionQueue() {
        UserAccount agent = agent("AGT-CLAIM-5", "claim5@test.local");
        PrintRequest request = paid("PR-CLAIM-7");
        service.claimNext(agent.getId());
        ReportProblemRequest problem = new ReportProblemRequest();
        problem.setExceptionType(RequestExceptionType.PRINTER_INCOMPATIBILITY);
        problem.setReason("Printer cannot process this document");
        service.reportProblem(request.getId(), agent.getId(), problem);
        assertEquals(PrintRequestStatus.FILE_ERROR,
                requests.findById(request.getId()).orElseThrow().getStatus());
        assertEquals(1, exceptions.findByPrintRequest_IdAndResolvedAtIsNull(request.getId()).size());
    }

    private UserAccount agent(String agentId, String email) {
        UserAccount account = new UserAccount();
        account.setFullName("Processing Agent");
        account.setAgentId(agentId);
        account.setEmail(email);
        account.setContactNumber("018" + Math.abs(agentId.hashCode() % 100000000));
        account.setPasswordHash("hash");
        account.setRole(AccountRole.AGENT);
        account.setAgentStatus(AgentStatus.APPROVED);
        return accounts.saveAndFlush(account);
    }

    private PrintRequest paid(String tracking) {
        PrintRequest request = new PrintRequest();
        request.setTrackingNumber(tracking);
        request.setFullName("Processing User");
        request.setRegistrationNumber("242-15-125");
        request.setUserType(UserType.STUDENT);
        request.setPrintColor(PrintColor.BLACK_AND_WHITE);
        request.setPrintingSide(PrintingSide.SINGLE_SIDED);
        request.setCopies(1);
        request.setStatus(PrintRequestStatus.PAID);
        request.setPaymentStatus(PaymentStatus.PAID);
        request.setPricingConfigId(1L);
        request.setPricingRevision(1);
        request.setAppliedRate(new BigDecimal("2.00"));
        request.setTotalDocumentPages(1);
        request.setTotalPhysicalSheets(1);
        request.setTotalAmount(new BigDecimal("2.00"));
        request.setCalculatedAt(LocalDateTime.now());
        request.setExpiresAt(LocalDateTime.now().plusHours(12));
        return requests.saveAndFlush(request);
    }

    private PrintRequestFile file(PrintRequest request) {
        PrintRequestFile file = new PrintRequestFile();
        file.setPrintRequest(request);
        file.setOriginalFileName("document.pdf");
        file.setFileType(PrintFileType.PDF);
        file.setMimeType("application/pdf");
        file.setFileSizeBytes(3);
        file.setPageCount(1);
        file.setPhysicalSheetsPerCopy(1);
        file.setTotalPhysicalSheets(1);
        file.setCopies(1);
        file.setAppliedRate(new BigDecimal("2.00"));
        file.setSubtotal(new BigDecimal("2.00"));
        file.setCalculatedAt(LocalDateTime.now());
        file.setFileData(new byte[]{1, 2, 3});
        return files.saveAndFlush(file);
    }
}