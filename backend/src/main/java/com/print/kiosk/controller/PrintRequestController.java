package com.print.kiosk.controller;

import com.print.kiosk.dto.request.CancelPrintRequestRequest;
import com.print.kiosk.dto.request.CreatePrintRequestRequest;
import com.print.kiosk.dto.response.PrintRequestPreviewResponse;
import com.print.kiosk.dto.response.PrintRequestResponse;
import com.print.kiosk.service.interfaces.PrintRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/print-requests")
public class PrintRequestController {

    private final PrintRequestService printRequestService;

    public PrintRequestController(PrintRequestService printRequestService) {
        this.printRequestService = printRequestService;
    }

    @PostMapping(
            value = "/preview",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<PrintRequestPreviewResponse> preview(
            @Valid @RequestPart("request") CreatePrintRequestRequest request,
            @RequestPart("files") List<MultipartFile> files
    ) {
        return ResponseEntity.ok(printRequestService.preview(request, files));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PrintRequestResponse> createPrintRequest(
            @Valid @RequestPart("request") CreatePrintRequestRequest request,
            @RequestPart("files") List<MultipartFile> files
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(printRequestService.createPrintRequest(request, files));
    }

    @GetMapping("/{trackingNumber}")
    public ResponseEntity<PrintRequestResponse> getByTrackingNumber(
            @PathVariable String trackingNumber
    ) {
        return ResponseEntity.ok(printRequestService.getByTrackingNumber(trackingNumber));
    }

    @PostMapping("/{trackingNumber}/cancel")
    public ResponseEntity<PrintRequestResponse> cancel(
            @PathVariable String trackingNumber,
            @Valid @RequestBody CancelPrintRequestRequest request
    ) {
        return ResponseEntity.ok(printRequestService.cancel(trackingNumber, request));
    }
}