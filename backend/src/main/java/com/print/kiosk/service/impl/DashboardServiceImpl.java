package com.print.kiosk.service.impl;

import com.print.kiosk.dto.response.AdminDashboardResponse;
import com.print.kiosk.dto.response.AdminReportResponse;
import com.print.kiosk.dto.response.AgentDashboardResponse;
import com.print.kiosk.dto.response.AgentPerformanceResponse;
import com.print.kiosk.dto.response.PrintStatisticsResponse;
import com.print.kiosk.dto.response.RevenueWindowResponse;
import com.print.kiosk.dto.response.SalesBreakdownResponse;
import com.print.kiosk.entity.AccountRole;
import com.print.kiosk.entity.PrintColor;
import com.print.kiosk.entity.PrintFileType;
import com.print.kiosk.entity.PrintRequest;
import com.print.kiosk.entity.PrintRequestStatus;
import com.print.kiosk.entity.ReportingPeriod;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.exception.ResourceNotFoundException;
import com.print.kiosk.repository.CategorySalesProjection;
import com.print.kiosk.repository.DashboardPaymentRecordRepository;
import com.print.kiosk.repository.DashboardPrintRequestFileRepository;
import com.print.kiosk.repository.DashboardPrintRequestRepository;
import com.print.kiosk.repository.DashboardRequestExceptionRepository;
import com.print.kiosk.repository.InventoryTransactionRepository;
import com.print.kiosk.repository.ItemSalesProjection;
import com.print.kiosk.repository.UserAccountRepository;
import com.print.kiosk.service.interfaces.DashboardService;
import com.print.kiosk.service.model.ReportingWindow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Set<PrintRequestStatus> ACTIVE_STATUSES = EnumSet.of(
            PrintRequestStatus.AWAITING_PAYMENT,
            PrintRequestStatus.PAID,
            PrintRequestStatus.CLAIMED,
            PrintRequestStatus.DOWNLOADING,
            PrintRequestStatus.PRINTING
    );

    private static final List<PrintFileType> PDF_TYPES =
            List.of(PrintFileType.PDF);

    private static final List<PrintFileType> DOCUMENT_TYPES =
            List.of(PrintFileType.DOC, PrintFileType.DOCX);

    private static final List<PrintFileType> IMAGE_TYPES = List.of(
            PrintFileType.JPG,
            PrintFileType.JPEG,
            PrintFileType.PNG,
            PrintFileType.HEIC,
            PrintFileType.HEIF
    );

    private final DashboardPrintRequestRepository printRequestRepository;
    private final DashboardPrintRequestFileRepository printFileRepository;
    private final DashboardPaymentRecordRepository paymentRecordRepository;
    private final DashboardRequestExceptionRepository requestExceptionRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final UserAccountRepository userAccountRepository;
    private final ReportingTimeService reportingTimeService;

    public DashboardServiceImpl(
            DashboardPrintRequestRepository printRequestRepository,
            DashboardPrintRequestFileRepository printFileRepository,
            DashboardPaymentRecordRepository paymentRecordRepository,
            DashboardRequestExceptionRepository requestExceptionRepository,
            InventoryTransactionRepository inventoryTransactionRepository,
            UserAccountRepository userAccountRepository,
            ReportingTimeService reportingTimeService
    ) {
        this.printRequestRepository = printRequestRepository;
        this.printFileRepository = printFileRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.requestExceptionRepository = requestExceptionRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.userAccountRepository = userAccountRepository;
        this.reportingTimeService = reportingTimeService;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse adminDashboard(LocalDate referenceDate) {
        LocalDate date = referenceDate == null
                ? reportingTimeService.today()
                : referenceDate;

        ReportingWindow day = reportingTimeService.window(
                ReportingPeriod.DAILY,
                date
        );
        ReportingWindow week = reportingTimeService.window(
                ReportingPeriod.WEEKLY,
                date
        );
        ReportingWindow month = reportingTimeService.window(
                ReportingPeriod.MONTHLY,
                date
        );

        return new AdminDashboardResponse(
                activeQueueCount(),
                completedCount(day),
                printStatistics(day),
                new RevenueWindowResponse(
                        printingRevenue(day),
                        printingRevenue(week),
                        printingRevenue(month)
                ),
                new RevenueWindowResponse(
                        inventoryRevenue(day),
                        inventoryRevenue(week),
                        inventoryRevenue(month)
                ),
                categorySales(month),
                itemSales(month),
                performance(month),
                reportingTimeService.now()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminReportResponse adminReport(
            ReportingPeriod period,
            LocalDate referenceDate
    ) {
        if (period == null) {
            throw new IllegalArgumentException("Reporting period is required");
        }

        ReportingWindow window = reportingTimeService.window(
                period,
                referenceDate
        );

        return new AdminReportResponse(
                period,
                window.start(),
                window.endExclusive(),
                activeQueueCount(),
                completedCount(window),
                printStatistics(window),
                printingRevenue(window),
                inventoryRevenue(window),
                categorySales(window),
                itemSales(window),
                performance(window),
                reportingTimeService.now()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentPerformanceResponse> agentPerformance(
            ReportingPeriod period,
            LocalDate referenceDate
    ) {
        if (period == null) {
            throw new IllegalArgumentException("Reporting period is required");
        }
        return performance(reportingTimeService.window(period, referenceDate));
    }

    @Override
    @Transactional(readOnly = true)
    public AgentDashboardResponse agentDashboard(
            Long agentAccountId,
            LocalDate referenceDate
    ) {
        UserAccount agent = userAccountRepository.findById(agentAccountId)
                .filter(account -> account.getRole() == AccountRole.AGENT)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Agent account was not found"
                ));

        LocalDate date = referenceDate == null
                ? reportingTimeService.today()
                : referenceDate;
        ReportingWindow day = reportingTimeService.window(
                ReportingPeriod.DAILY,
                date
        );
        ReportingWindow week = reportingTimeService.window(
                ReportingPeriod.WEEKLY,
                date
        );
        ReportingWindow month = reportingTimeService.window(
                ReportingPeriod.MONTHLY,
                date
        );

        List<PrintRequest> completedThisMonth = printRequestRepository
                .findCompletedWithAssignedAgentBetween(
                        month.start(),
                        month.endExclusive()
                )
                .stream()
                .filter(request -> request.getAssignedAgent() != null
                        && request.getAssignedAgent().getId().equals(agentAccountId))
                .toList();

        return new AgentDashboardResponse(
                agent.getId(),
                agent.getAgentId(),
                agent.getFullName(),
                activeQueueCount(),
                completedCount(day),
                completedByAgent(agentAccountId, day),
                completedByAgent(agentAccountId, week),
                completedByAgent(agentAccountId, month),
                averageProcessingMinutes(completedThisMonth),
                requestExceptionRepository.countByAgentBetween(
                        agentAccountId,
                        month.start(),
                        month.endExclusive()
                ),
                inventoryActionCount(agentAccountId, day),
                inventoryActionCount(agentAccountId, week),
                inventoryActionCount(agentAccountId, month),
                inventoryAmount(agentAccountId, day),
                inventoryAmount(agentAccountId, week),
                inventoryAmount(agentAccountId, month),
                reportingTimeService.now()
        );
    }

    private long activeQueueCount() {
        return printRequestRepository.countByStatusIn(ACTIVE_STATUSES);
    }

    private long completedCount(ReportingWindow window) {
        return printRequestRepository
                .countByStatusAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
                        PrintRequestStatus.COMPLETED,
                        window.start(),
                        window.endExclusive()
                );
    }

    private long completedByAgent(
            Long agentId,
            ReportingWindow window
    ) {
        return printRequestRepository.countCompletedByAgentBetween(
                agentId,
                window.start(),
                window.endExclusive()
        );
    }

    private PrintStatisticsResponse printStatistics(ReportingWindow window) {
        long colorPages = value(printRequestRepository.sumDocumentPagesByColor(
                PrintColor.COLOR,
                window.start(),
                window.endExclusive()
        ));
        long blackWhitePages = value(printRequestRepository.sumDocumentPagesByColor(
                PrintColor.BLACK_AND_WHITE,
                window.start(),
                window.endExclusive()
        ));
        long colorSheets = value(printRequestRepository.sumPhysicalSheetsByColor(
                PrintColor.COLOR,
                window.start(),
                window.endExclusive()
        ));
        long blackWhiteSheets = value(printRequestRepository.sumPhysicalSheetsByColor(
                PrintColor.BLACK_AND_WHITE,
                window.start(),
                window.endExclusive()
        ));

        long pdfFiles = printFileRepository.countCompletedFilesByTypes(
                PDF_TYPES,
                window.start(),
                window.endExclusive()
        );
        long documentFiles = printFileRepository.countCompletedFilesByTypes(
                DOCUMENT_TYPES,
                window.start(),
                window.endExclusive()
        );
        long imageFiles = printFileRepository.countCompletedFilesByTypes(
                IMAGE_TYPES,
                window.start(),
                window.endExclusive()
        );

        long totalPages = colorPages + blackWhitePages;
        long totalSheets = colorSheets + blackWhiteSheets;
        long totalFiles = pdfFiles + documentFiles + imageFiles;

        return new PrintStatisticsResponse(
                colorPages,
                blackWhitePages,
                totalPages,
                colorSheets,
                blackWhiteSheets,
                totalSheets,
                pdfFiles,
                documentFiles,
                imageFiles,
                totalFiles,
                percentage(colorPages, totalPages),
                percentage(blackWhitePages, totalPages),
                percentage(pdfFiles, totalFiles),
                percentage(documentFiles, totalFiles),
                percentage(imageFiles, totalFiles)
        );
    }

    private BigDecimal printingRevenue(ReportingWindow window) {
        return money(paymentRecordRepository.sumPaidAmountBetween(
                window.start(),
                window.endExclusive()
        ));
    }

    private BigDecimal inventoryRevenue(ReportingWindow window) {
        return money(inventoryTransactionRepository.sumAmountBetween(
                window.start(),
                window.endExclusive()
        ));
    }

    private long inventoryActionCount(
            Long agentId,
            ReportingWindow window
    ) {
        return inventoryTransactionRepository
                .countByAgent_IdAndTransactedAtGreaterThanEqualAndTransactedAtLessThan(
                        agentId,
                        window.start(),
                        window.endExclusive()
                );
    }

    private BigDecimal inventoryAmount(
            Long agentId,
            ReportingWindow window
    ) {
        return money(inventoryTransactionRepository.sumAmountByAgentBetween(
                agentId,
                window.start(),
                window.endExclusive()
        ));
    }

    private List<SalesBreakdownResponse> categorySales(ReportingWindow window) {
        return inventoryTransactionRepository
                .summarizeByCategory(window.start(), window.endExclusive())
                .stream()
                .map(this::categoryResponse)
                .toList();
    }

    private List<SalesBreakdownResponse> itemSales(ReportingWindow window) {
        return inventoryTransactionRepository
                .summarizeByItem(window.start(), window.endExclusive())
                .stream()
                .map(this::itemResponse)
                .toList();
    }

    private SalesBreakdownResponse categoryResponse(
            CategorySalesProjection projection
    ) {
        return new SalesBreakdownResponse(
                projection.getLabel(),
                projection.getNetQuantity() == null
                        ? 0
                        : projection.getNetQuantity(),
                money(projection.getNetAmount())
        );
    }

    private SalesBreakdownResponse itemResponse(ItemSalesProjection projection) {
        return new SalesBreakdownResponse(
                projection.getLabel(),
                projection.getNetQuantity() == null
                        ? 0
                        : projection.getNetQuantity(),
                money(projection.getNetAmount())
        );
    }

    private List<AgentPerformanceResponse> performance(ReportingWindow window) {
        List<UserAccount> agents = userAccountRepository.findAll()
                .stream()
                .filter(account -> account.getRole() == AccountRole.AGENT)
                .toList();

        List<PrintRequest> completed = printRequestRepository
                .findCompletedWithAssignedAgentBetween(
                        window.start(),
                        window.endExclusive()
                );

        Map<Long, List<PrintRequest>> byAgent = new HashMap<>();
        for (PrintRequest request : completed) {
            if (request.getAssignedAgent() != null) {
                byAgent.computeIfAbsent(
                        request.getAssignedAgent().getId(),
                        ignored -> new ArrayList<>()
                ).add(request);
            }
        }

        return agents.stream()
                .map(agent -> {
                    List<PrintRequest> ownCompleted = byAgent.getOrDefault(
                            agent.getId(),
                            List.of()
                    );
                    return new AgentPerformanceResponse(
                            agent.getId(),
                            agent.getAgentId(),
                            agent.getFullName(),
                            ownCompleted.size(),
                            averageProcessingMinutes(ownCompleted),
                            requestExceptionRepository.countByAgentBetween(
                                    agent.getId(),
                                    window.start(),
                                    window.endExclusive()
                            ),
                            inventoryActionCount(agent.getId(), window),
                            inventoryAmount(agent.getId(), window)
                    );
                })
                .sorted(
                        Comparator.comparingLong(
                                        AgentPerformanceResponse::completedJobs
                                )
                                .reversed()
                                .thenComparing(
                                        AgentPerformanceResponse::fullName,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                )
                .toList();
    }

    private BigDecimal averageProcessingMinutes(
            List<PrintRequest> completedRequests
    ) {
        long totalSeconds = 0;
        long counted = 0;

        for (PrintRequest request : completedRequests) {
            LocalDateTime start = request.getClaimedAt() != null
                    ? request.getClaimedAt()
                    : request.getPrintingStartedAt();
            LocalDateTime end = request.getCompletedAt();

            if (start != null && end != null && !end.isBefore(start)) {
                totalSeconds += Duration.between(start, end).getSeconds();
                counted++;
            }
        }

        if (counted == 0) {
            return BigDecimal.ZERO.setScale(2);
        }

        return BigDecimal.valueOf(totalSeconds)
                .divide(BigDecimal.valueOf(60L * counted), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentage(long part, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(part)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private long value(Long value) {
        return value == null ? 0 : value;
    }

    private BigDecimal money(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(2)
                : value.setScale(2, RoundingMode.HALF_UP);
    }
}