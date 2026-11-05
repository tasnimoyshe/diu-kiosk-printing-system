package com.print.kiosk.service.interfaces;

import com.print.kiosk.dto.request.UpdatePrintingPriceRequest;
import com.print.kiosk.dto.response.PriceHistoryResponse;
import com.print.kiosk.dto.response.PrintingPriceResponse;
import com.print.kiosk.service.model.PricingSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PricingService {
    PrintingPriceResponse getCurrentPricing();

    PricingSnapshot getCurrentSnapshot();

    PrintingPriceResponse updatePricing(
            UpdatePrintingPriceRequest request,
            Long agentAccountId
    );

    Page<PriceHistoryResponse> getPriceHistory(Pageable pageable);
}