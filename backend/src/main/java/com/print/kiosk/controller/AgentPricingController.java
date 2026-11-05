package com.print.kiosk.controller;

import com.print.kiosk.dto.request.UpdatePrintingPriceRequest;
import com.print.kiosk.dto.response.PriceHistoryResponse;
import com.print.kiosk.dto.response.PrintingPriceResponse;
import com.print.kiosk.service.interfaces.PricingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent/pricing")
public class AgentPricingController {

    private final PricingService pricingService;

    public AgentPricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping
    public ResponseEntity<PrintingPriceResponse> getCurrentPricing() {
        return ResponseEntity.ok(pricingService.getCurrentPricing());
    }

    @PatchMapping
    public ResponseEntity<PrintingPriceResponse> updatePricing(
            @Valid @RequestBody UpdatePrintingPriceRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                pricingService.updatePricing(request, accountId(jwt))
        );
    }

    @GetMapping("/history")
    public ResponseEntity<Page<PriceHistoryResponse>> getHistory(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(pricingService.getPriceHistory(pageable));
    }

    private Long accountId(Jwt jwt) {
        Number claim = jwt.getClaim("accountId");
        if (claim == null) {
            throw new IllegalArgumentException("Authenticated account ID is missing");
        }
        return claim.longValue();
    }
}