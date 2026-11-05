package com.print.kiosk.service.interfaces;

import com.print.kiosk.dto.request.CancelPrintRequestRequest;
import com.print.kiosk.dto.request.CreatePrintRequestRequest;
import com.print.kiosk.dto.response.PrintRequestPreviewResponse;
import com.print.kiosk.dto.response.PrintRequestResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PrintRequestService {

    PrintRequestPreviewResponse preview(
            CreatePrintRequestRequest request,
            List<MultipartFile> files
    );

    PrintRequestResponse createPrintRequest(
            CreatePrintRequestRequest request,
            List<MultipartFile> files
    );

    PrintRequestResponse getByTrackingNumber(String trackingNumber);

    PrintRequestResponse cancel(
            String trackingNumber,
            CancelPrintRequestRequest request
    );
}