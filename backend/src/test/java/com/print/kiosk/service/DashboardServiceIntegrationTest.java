package com.print.kiosk.service;

import com.print.kiosk.dto.request.CreateInventoryItemRequest;
import com.print.kiosk.dto.response.AdminReportResponse;
import com.print.kiosk.dto.response.AgentDashboardResponse;
import com.print.kiosk.dto.response.InventoryItemResponse;
import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.AgentStatus;
import com.print.kiosk.entity.PaymentMethod;
import com.print.kiosk.entity.PaymentRecord;
import com.print.kiosk.entity.PaymentStatus;
import com.print.kiosk.entity.PrintColor;
import com.print.kiosk.entity.PrintFileType;
import com.print.kiosk.entity.PrintRequest;
import com.print.kiosk.entity.PrintRequestFile;
import com.print.kiosk.entity.PrintRequestStatus;
import com.print.kiosk.entity.PrintingSide;
import com.print.kiosk.entity.ReportingPeriod;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.entity.UserType;
import com.print.kiosk.repository.PaymentRecordRepository;
import com.print.kiosk.repository.PrintRequestFileRepository;
import com.print.kiosk.repository.PrintRequestRepository;
import com.print.kiosk.repository.UserAccountRepository;
import com.print.kiosk.service.interfaces.DashboardService;
import com.print.kiosk.service.interfaces.InventoryItemService;
import com.print.kiosk.service.interfaces.InventoryTransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DashboardServiceIntegrationTest {

    private static final ZoneId DHAKA = ZoneId.of("Asia/Dhaka");

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private InventoryItemService itemService;

    @Autowired
    private InventoryTransactionService transactionService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PrintRequestRepository printRequestRepository;

    @Autowired
    private PrintRequestFileRepository printRequestFileRepository;

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    @Test
    void adminReportCombinesPrintingInventoryAndFileStatistics() {
        UserAccount agent = agent();
        createInventorySale(agent, "30.00");
        createCompletedPrintRequest(agent, "20.00");

        AdminReportResponse report = dashboardService.adminReport(
                ReportingPeriod.DAILY,
                LocalDate.now(DHAKA)
        );

        assertTrue(report.completedRequests() >= 1);
        assertTrue(report.printingRevenue().compareTo(new BigDecimal("20.00")) >= 0);
        assertTrue(report.inventoryRevenue().compareTo(new BigDecimal("30.00")) >= 0);
        assertTrue(report.printStatistics().colorDocumentPages() >= 4);
        assertTrue(report.printStatistics().colorPhysicalSheets() >= 2);
        assertTrue(report.printStatistics().pdfFiles() >= 1);
    }

    @Test
    void agentDashboardContainsOnlyTheAgentsOwnPerformanceAndInventoryActivity() {
        UserAccount agent = agent();
        createInventorySale(agent, "35.00");
        createCompletedPrintRequest(agent, "25.00");

        AgentDashboardResponse dashboard = dashboardService.agentDashboard(
                agent.getId(),
                LocalDate.now(DHAKA)
        );

        assertEquals(agent.getId(), dashboard.agentAccountId());
        assertTrue(dashboard.ownCompletedToday() >= 1);
        assertTrue(dashboard.ownInventoryActionsToday() >= 1);
        assertTrue(
                dashboard.ownInventoryNetAmountToday()
                        .compareTo(new BigDecimal("35.00")) >= 0
        );
    }

    private UserAccount agent() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        UserAccount account = new UserAccount();
        account.setFullName("Dashboard Agent");
        account.setAgentId("AGT-DASH-" + suffix);
        account.setEmail("dashboard-" + suffix + "@test.local");
        account.setContactNumber("018" + Math.abs(suffix.hashCode() % 100000000));
        account.setPasswordHash("test-password-hash");
        account.setRole(AccountRole.AGENT);
        account.setAgentStatus(AgentStatus.APPROVED);
        return userAccountRepository.saveAndFlush(account);
    }

    private void createInventorySale(UserAccount agent, String price) {
        CreateInventoryItemRequest request = new CreateInventoryItemRequest();
        request.setName("Dashboard Item " + UUID.randomUUID().toString().substring(0, 5));
        request.setCategoryName("Dashboard Category " + UUID.randomUUID().toString().substring(0, 5));
        request.setPrice(new BigDecimal(price));
        InventoryItemResponse item = itemService.create(agent.getId(), request);
        transactionService.recordSale(item.id(), agent.getId());
    }

    private void createCompletedPrintRequest(
            UserAccount agent,
            String amount
    ) {
        LocalDateTime now = LocalDateTime.now(DHAKA);
        PrintRequest request = new PrintRequest();
        request.setTrackingNumber(
                "PR-DASH-" + UUID.randomUUID().toString().substring(0, 10)
        );
        request.setFullName("Dashboard User");
        request.setRegistrationNumber("242-15-125");
        request.setUserType(UserType.STUDENT);
        request.setDepartment("CSE");
        request.setPrintColor(PrintColor.COLOR);
        request.setPrintingSide(PrintingSide.DOUBLE_SIDED);
        request.setCopies(1);
        request.setStatus(PrintRequestStatus.COMPLETED);
        request.setPaymentStatus(PaymentStatus.PAID);
        request.setAssignedAgent(agent);
        request.setClaimedAt(now.minusMinutes(15));
        request.setPrintingStartedAt(now.minusMinutes(10));
        request.setCompletedAt(now.minusMinutes(1));
        request.setPricingConfigId(1L);
        request.setPricingRevision(1);
        request.setAppliedRate(new BigDecimal("10.00"));
        request.setTotalDocumentPages(4);
        request.setTotalPhysicalSheets(2);
        request.setTotalAmount(new BigDecimal(amount));
        request.setCalculatedAt(now.minusMinutes(20));
        request.setExpiresAt(now.plusHours(12));
        request = printRequestRepository.saveAndFlush(request);

        PrintRequestFile file = new PrintRequestFile();
        file.setPrintRequest(request);
        file.setOriginalFileName("dashboard.pdf");
        file.setFileType(PrintFileType.PDF);
        file.setMimeType("application/pdf");
        file.setFileSizeBytes(10);
        file.setPageCount(4);
        file.setPhysicalSheetsPerCopy(2);
        file.setTotalPhysicalSheets(2);
        file.setCopies(1);
        file.setAppliedRate(new BigDecimal("10.00"));
        file.setSubtotal(new BigDecimal(amount));
        file.setCalculatedAt(now.minusMinutes(20));
        file.setFileData(new byte[]{1, 2, 3});
        printRequestFileRepository.saveAndFlush(file);

        PaymentRecord payment = new PaymentRecord();
        payment.setPrintRequest(request);
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setAmount(new BigDecimal(amount));
        payment.setStatus(PaymentStatus.PAID);
        payment.setCompletedAt(now.minusMinutes(18));
        paymentRecordRepository.saveAndFlush(payment);
    }
}