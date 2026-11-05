package com.print.kiosk.service.interfaces;

import com.print.kiosk.dto.request.InventoryReasonRequest;
import com.print.kiosk.dto.request.ManualInventoryCorrectionRequest;
import com.print.kiosk.dto.response.InventoryTransactionResponse;
import com.print.kiosk.entity.InventoryTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface InventoryTransactionService {

    InventoryTransactionResponse recordSale(Long itemId, Long agentAccountId);

    InventoryTransactionResponse recordReturn(
            Long itemId,
            Long agentAccountId,
            InventoryReasonRequest request
    );

    InventoryTransactionResponse recordCorrection(
            Long itemId,
            Long agentAccountId,
            ManualInventoryCorrectionRequest request
    );

    Page<InventoryTransactionResponse> search(
            Long itemId,
            Long categoryId,
            InventoryTransactionType transactionType,
            Long agentAccountId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );
}