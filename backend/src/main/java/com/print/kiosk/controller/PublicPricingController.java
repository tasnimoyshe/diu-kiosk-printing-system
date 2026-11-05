package com.print.kiosk.controller;

import com.print.kiosk.dto.response.PrintingPriceResponse;
import com.print.kiosk.service.interfaces.PricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/print-requests/pricing")
public class PublicPricingController {

    private final PricingService pricingService;

    public PublicPricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping
    public ResponseEntity<PrintingPriceResponse> getCurrentPricing() {
        return ResponseEntity.ok(pricingService.getCurrentPricing());
    }
}