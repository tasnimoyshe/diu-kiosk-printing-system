package com.print.kiosk.controller;

import com.print.kiosk.dto.request.CreateInventoryCategoryRequest;
import com.print.kiosk.dto.request.MergeInventoryCategoryRequest;
import com.print.kiosk.dto.request.RenameInventoryCategoryRequest;
import com.print.kiosk.dto.response.InventoryCategoryResponse;
import com.print.kiosk.service.interfaces.InventoryCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/agent/inventory/categories")
public class AgentInventoryCategoryController {

    private final InventoryCategoryService categoryService;

    public AgentInventoryCategoryController(
            InventoryCategoryService categoryService
    ) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryCategoryResponse>> list(
            @RequestParam(defaultValue = "false") boolean includeArchived
    ) {
        return ResponseEntity.ok(categoryService.list(includeArchived));
    }

    @PostMapping
    public ResponseEntity<InventoryCategoryResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateInventoryCategoryRequest request
    ) {
        return ResponseEntity.ok(
                categoryService.create(accountId(jwt), request)
        );
    }

    @PatchMapping("/{categoryId}/rename")
    public ResponseEntity<InventoryCategoryResponse> rename(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RenameInventoryCategoryRequest request
    ) {
        return ResponseEntity.ok(
                categoryService.rename(
                        categoryId,
                        accountId(jwt),
                        request
                )
        );
    }

    @PostMapping("/{sourceCategoryId}/merge")
    public ResponseEntity<InventoryCategoryResponse> merge(
            @PathVariable Long sourceCategoryId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MergeInventoryCategoryRequest request
    ) {
        return ResponseEntity.ok(
                categoryService.merge(
                        sourceCategoryId,
                        accountId(jwt),
                        request
                )
        );
    }

    @PatchMapping("/{categoryId}/archive")
    public ResponseEntity<InventoryCategoryResponse> archive(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                categoryService.archive(categoryId, accountId(jwt))
        );
    }

    @PatchMapping("/{categoryId}/reactivate")
    public ResponseEntity<InventoryCategoryResponse> reactivate(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                categoryService.reactivate(categoryId, accountId(jwt))
        );
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<InventoryCategoryResponse> delete(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                categoryService.delete(categoryId, accountId(jwt))
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