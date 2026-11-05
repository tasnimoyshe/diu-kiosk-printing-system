package com.print.kiosk.controller;

import com.print.kiosk.dto.request.InventoryReasonRequest;
import com.print.kiosk.dto.request.ManualInventoryCorrectionRequest;
import com.print.kiosk.dto.response.InventoryTransactionResponse;
import com.print.kiosk.entity.InventoryTransactionType;
import com.print.kiosk.service.interfaces.InventoryTransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/agent/inventory")
public class AgentInventoryTransactionController {

    private final InventoryTransactionService transactionService;

    public AgentInventoryTransactionController(
            InventoryTransactionService transactionService
    ) {
        this.transactionService = transactionService;
    }

    @PostMapping("/items/{itemId}/sale")
    public ResponseEntity<InventoryTransactionResponse> sale(
            @PathVariable Long itemId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                transactionService.recordSale(itemId, accountId(jwt))
        );
    }

    @PostMapping("/items/{itemId}/return")
    public ResponseEntity<InventoryTransactionResponse> returnItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InventoryReasonRequest request
    ) {
        return ResponseEntity.ok(
                transactionService.recordReturn(
                        itemId,
                        accountId(jwt),
                        request
                )
        );
    }

    @PostMapping("/items/{itemId}/correction")
    public ResponseEntity<InventoryTransactionResponse> correction(
            @PathVariable Long itemId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ManualInventoryCorrectionRequest request
    ) {
        return ResponseEntity.ok(
                transactionService.recordCorrection(
                        itemId,
                        accountId(jwt),
                        request
                )
        );
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<InventoryTransactionResponse>> transactions(
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) InventoryTransactionType transactionType,
            @RequestParam(required = false) Long agentAccountId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                transactionService.search(
                        itemId,
                        categoryId,
                        transactionType,
                        agentAccountId,
                        from,
                        to,
                        pageable
                )
        );
    }

    @GetMapping("/transactions/my")
    public ResponseEntity<Page<InventoryTransactionResponse>> myTransactions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) InventoryTransactionType transactionType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                transactionService.search(
                        itemId,
                        categoryId,
                        transactionType,
                        accountId(jwt),
                        from,
                        to,
                        pageable
                )
        );
    }

    private Long accountId(Jwt jwt) {
        Number value = jwt.getClaim("accountId");
        if (value == null) {
            throw new IllegalArgumentException(
                    "Authenticated account ID is missing"
            );
        }
        return value.longValue();
    }
}