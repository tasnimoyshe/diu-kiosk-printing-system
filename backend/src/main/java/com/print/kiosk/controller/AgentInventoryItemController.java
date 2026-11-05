package com.print.kiosk.controller;

import com.print.kiosk.dto.request.ChangeInventoryItemPriceRequest;
import com.print.kiosk.dto.request.CreateInventoryItemRequest;
import com.print.kiosk.dto.request.RenameInventoryItemRequest;
import com.print.kiosk.dto.response.InventoryItemPriceHistoryResponse;
import com.print.kiosk.dto.response.InventoryItemResponse;
import com.print.kiosk.service.interfaces.InventoryItemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/agent/inventory/items")
public class AgentInventoryItemController {

    private final InventoryItemService itemService;

    public AgentInventoryItemController(InventoryItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<Page<InventoryItemResponse>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                itemService.list(
                        categoryId,
                        search,
                        includeArchived,
                        pageable
                )
        );
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<InventoryItemResponse> get(
            @PathVariable Long itemId
    ) {
        return ResponseEntity.ok(itemService.get(itemId));
    }

    @PostMapping
    public ResponseEntity<InventoryItemResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateInventoryItemRequest request
    ) {
        return ResponseEntity.ok(
                itemService.create(accountId(jwt), request)
        );
    }

    @PatchMapping("/{itemId}/rename")
    public ResponseEntity<InventoryItemResponse> rename(
            @PathVariable Long itemId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RenameInventoryItemRequest request
    ) {
        return ResponseEntity.ok(
                itemService.rename(itemId, accountId(jwt), request)
        );
    }

    @PatchMapping("/{itemId}/price")
    public ResponseEntity<InventoryItemResponse> changePrice(
            @PathVariable Long itemId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangeInventoryItemPriceRequest request
    ) {
        return ResponseEntity.ok(
                itemService.changePrice(itemId, accountId(jwt), request)
        );
    }

    @PatchMapping("/{itemId}/archive")
    public ResponseEntity<InventoryItemResponse> archive(
            @PathVariable Long itemId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                itemService.archive(itemId, accountId(jwt))
        );
    }

    @PatchMapping("/{itemId}/reactivate")
    public ResponseEntity<InventoryItemResponse> reactivate(
            @PathVariable Long itemId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                itemService.reactivate(itemId, accountId(jwt))
        );
    }

    @GetMapping("/{itemId}/price-history")
    public ResponseEntity<List<InventoryItemPriceHistoryResponse>> priceHistory(
            @PathVariable Long itemId
    ) {
        return ResponseEntity.ok(itemService.priceHistory(itemId));
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