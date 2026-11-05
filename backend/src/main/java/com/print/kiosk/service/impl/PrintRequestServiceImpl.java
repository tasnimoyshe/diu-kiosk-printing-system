package com.print.kiosk.service.impl;

import com.print.kiosk.dto.request.CancelPrintRequestRequest;
import com.print.kiosk.dto.request.CreatePrintRequestRequest;
import com.print.kiosk.dto.response.PrintFileResponse;
import com.print.kiosk.dto.response.PrintRequestPreviewResponse;
import com.print.kiosk.dto.response.PrintRequestResponse;
import com.print.kiosk.entity.PrintRequest;
import com.print.kiosk.entity.PrintRequestFile;
import com.print.kiosk.entity.PrintRequestStatus;
import com.print.kiosk.entity.PrintRequestStatusHistory;
import com.print.kiosk.entity.StatusActorType;
import com.print.kiosk.exception.InvalidPrintRequestStateException;
import com.print.kiosk.exception.ResourceNotFoundException;
import com.print.kiosk.mapper.PrintRequestMapper;
import com.print.kiosk.repository.PrintRequestFileRepository;
import com.print.kiosk.repository.PrintRequestRepository;
import com.print.kiosk.repository.PrintRequestStatusHistoryRepository;
import com.print.kiosk.service.interfaces.FileAnalysisService;
import com.print.kiosk.service.interfaces.PricingService;
import com.print.kiosk.service.interfaces.PrintRequestService;
import com.print.kiosk.service.model.AnalyzedUpload;
import com.print.kiosk.service.model.CalculatedPrintFile;
import com.print.kiosk.service.model.PricingCalculation;
import com.print.kiosk.service.model.PricingSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrintRequestServiceImpl implements PrintRequestService {

    private static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");

    private final PrintRequestRepository printRequestRepository;
    private final PrintRequestFileRepository printRequestFileRepository;
    private final PrintRequestStatusHistoryRepository statusHistoryRepository;
    private final PrintRequestMapper printRequestMapper;
    private final FileAnalysisService fileAnalysisService;
    private final PricingService pricingService;
    private final PricingCalculator pricingCalculator;
    private final TrackingNumberGenerator trackingNumberGenerator;
    private final long expirationHours;
    private final long fileDeletionDelayHours;

    public PrintRequestServiceImpl(
            PrintRequestRepository printRequestRepository,
            PrintRequestFileRepository printRequestFileRepository,
            PrintRequestStatusHistoryRepository statusHistoryRepository,
            PrintRequestMapper printRequestMapper,
            FileAnalysisService fileAnalysisService,
            PricingService pricingService,
            PricingCalculator pricingCalculator,
            TrackingNumberGenerator trackingNumberGenerator,
            @Value("${app.print-request.expiration-hours:12}") long expirationHours,
            @Value("${app.print-request.file-deletion-delay-hours:24}") long fileDeletionDelayHours
    ) {
        if (expirationHours <= 0 || fileDeletionDelayHours <= 0) {
            throw new IllegalStateException("Print-request expiration settings must be greater than zero");
        }
        this.printRequestRepository = printRequestRepository;
        this.printRequestFileRepository = printRequestFileRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.printRequestMapper = printRequestMapper;
        this.fileAnalysisService = fileAnalysisService;
        this.pricingService = pricingService;
        this.pricingCalculator = pricingCalculator;
        this.trackingNumberGenerator = trackingNumberGenerator;
        this.expirationHours = expirationHours;
        this.fileDeletionDelayHours = fileDeletionDelayHours;
    }

    @Override
    public PrintRequestPreviewResponse preview(
            CreatePrintRequestRequest request,
            List<MultipartFile> files
    ) {
        PricingCalculation calculation = calculate(request, files);
        return printRequestMapper.toPreviewResponse(request, calculation);
    }

    @Override
    @Transactional
    public PrintRequestResponse createPrintRequest(
            CreatePrintRequestRequest request,
            List<MultipartFile> files
    ) {
        PricingCalculation calculation = calculate(request, files);
        LocalDateTime now = LocalDateTime.now(DHAKA_ZONE);

        PrintRequest printRequest = printRequestMapper.toEntity(request, calculation);
        printRequest.setTrackingNumber(trackingNumberGenerator.nextTrackingNumber());
        printRequest.setStatus(PrintRequestStatus.AWAITING_PAYMENT);
        printRequest.setExpiresAt(now.plusHours(expirationHours));

        PrintRequest savedRequest = printRequestRepository.saveAndFlush(printRequest);
        List<PrintRequestFile> storedFiles = new ArrayList<>(calculation.files().size());

        for (CalculatedPrintFile calculatedFile : calculation.files()) {
            PrintRequestFile storedFile = new PrintRequestFile();
            storedFile.setPrintRequest(savedRequest);
            storedFile.setOriginalFileName(calculatedFile.originalFileName());
            storedFile.setFileType(calculatedFile.fileType());
            storedFile.setMimeType(calculatedFile.mimeType());
            storedFile.setFileSizeBytes(calculatedFile.fileSizeBytes());
            storedFile.setPageCount(calculatedFile.pageCount());
            storedFile.setPhysicalSheetsPerCopy(calculatedFile.physicalSheetsPerCopy());
            storedFile.setTotalPhysicalSheets(calculatedFile.totalPhysicalSheets());
            storedFile.setCopies(calculatedFile.copies());
            storedFile.setAppliedRate(calculatedFile.appliedRate());
            storedFile.setSubtotal(calculatedFile.subtotal());
            storedFile.setCalculatedAt(calculatedFile.calculatedAt());
            storedFile.setFileData(calculatedFile.fileData().clone());
            storedFiles.add(storedFile);
        }

        List<PrintRequestFile> savedFiles = printRequestFileRepository.saveAll(storedFiles);
        statusHistoryRepository.save(
                statusHistory(
                        savedRequest,
                        null,
                        PrintRequestStatus.AWAITING_PAYMENT,
                        StatusActorType.PUBLIC,
                        "Request submitted"
                )
        );

        List<PrintFileResponse> fileResponses = savedFiles.stream()
                .map(printRequestMapper::toSavedFileResponse)
                .toList();

        return printRequestMapper.toResponse(savedRequest, fileResponses);
    }

    @Override
    @Transactional
    public PrintRequestResponse getByTrackingNumber(String trackingNumber) {
        PrintRequest printRequest = findRequest(trackingNumber);
        expireIfDue(printRequest, LocalDateTime.now(DHAKA_ZONE));
        return response(printRequest);
    }

    @Override
    @Transactional(noRollbackFor = InvalidPrintRequestStateException.class)
    public PrintRequestResponse cancel(
            String trackingNumber,
            CancelPrintRequestRequest request
    ) {
        PrintRequest printRequest = findRequest(trackingNumber);

        if (!printRequest.getRegistrationNumber().equals(request.getRegistrationNumber().trim())) {
            throw new ResourceNotFoundException("Print request was not found or verification failed");
        }

        LocalDateTime now = LocalDateTime.now(DHAKA_ZONE);
        if (expireIfDue(printRequest, now)) {
            throw new InvalidPrintRequestStateException("This print request has expired and cannot be cancelled");
        }

        if (printRequest.getStatus() != PrintRequestStatus.AWAITING_PAYMENT) {
            throw new InvalidPrintRequestStateException(
                    "Only an unpaid request awaiting payment may be cancelled"
            );
        }

        String reason = normalizeReason(request.getReason(), "Cancelled by public user");
        printRequest.setStatus(PrintRequestStatus.CANCELLED);
        printRequest.setCancelledAt(now);
        printRequest.setCancellationReason(reason);
        printRequestRepository.saveAndFlush(printRequest);

        statusHistoryRepository.save(
                statusHistory(
                        printRequest,
                        PrintRequestStatus.AWAITING_PAYMENT,
                        PrintRequestStatus.CANCELLED,
                        StatusActorType.PUBLIC,
                        reason
                )
        );
        printRequestFileRepository.markDeletionEligibleByRequestId(
                printRequest.getId(),
                now.plusHours(fileDeletionDelayHours)
        );

        return response(printRequest);
    }

    private PricingCalculation calculate(
            CreatePrintRequestRequest request,
            List<MultipartFile> files
    ) {
        List<AnalyzedUpload> analyzedUploads = fileAnalysisService.analyze(files);
        PricingSnapshot snapshot = pricingService.getCurrentSnapshot();
        return pricingCalculator.calculate(
                analyzedUploads,
                request.getPrintColor(),
                request.getPrintingSide(),
                request.getCopies(),
                snapshot
        );
    }

    private PrintRequest findRequest(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("Tracking number is required");
        }

        return printRequestRepository
                .findByTrackingNumberForUpdate(trackingNumber.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Print request was not found"));
    }

    private boolean expireIfDue(PrintRequest printRequest, LocalDateTime now) {
        if (printRequest.getStatus() != PrintRequestStatus.AWAITING_PAYMENT
                || printRequest.getExpiresAt().isAfter(now)) {
            return false;
        }

        printRequest.setStatus(PrintRequestStatus.EXPIRED);
        printRequestRepository.saveAndFlush(printRequest);
        statusHistoryRepository.save(
                statusHistory(
                        printRequest,
                        PrintRequestStatus.AWAITING_PAYMENT,
                        PrintRequestStatus.EXPIRED,
                        StatusActorType.SYSTEM,
                        "Unpaid request expired after " + expirationHours + " hours"
                )
        );
        printRequestFileRepository.markDeletionEligibleByRequestId(
                printRequest.getId(),
                now.plusHours(fileDeletionDelayHours)
        );
        return true;
    }

    private PrintRequestStatusHistory statusHistory(
            PrintRequest printRequest,
            PrintRequestStatus previousStatus,
            PrintRequestStatus newStatus,
            StatusActorType actorType,
            String reason
    ) {
        PrintRequestStatusHistory history = new PrintRequestStatusHistory();
        history.setPrintRequest(printRequest);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setActorType(actorType);
        history.setActorAccountId(null);
        history.setReason(reason);
        return history;
    }

    private PrintRequestResponse response(PrintRequest printRequest) {
        List<PrintFileResponse> files = printRequestFileRepository
                .findMetadataByPrintRequestId(printRequest.getId())
                .stream()
                .map(printRequestMapper::toFileResponse)
                .toList();
        return printRequestMapper.toResponse(printRequest, files);
    }

    private String normalizeReason(String reason, String defaultReason) {
        if (reason == null || reason.isBlank()) {
            return defaultReason;
        }
        return reason.trim();
    }
}