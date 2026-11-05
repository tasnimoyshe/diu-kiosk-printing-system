package com.print.kiosk.service.impl;

import com.print.kiosk.dto.request.InventoryReasonRequest;
import com.print.kiosk.dto.request.ManualInventoryCorrectionRequest;
import com.print.kiosk.dto.response.InventoryTransactionResponse;
import com.print.kiosk.entity.InventoryCategory;
import com.print.kiosk.entity.InventoryItem;
import com.print.kiosk.entity.InventoryTransaction;
import com.print.kiosk.entity.InventoryTransactionType;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.exception.InventoryConflictException;
import com.print.kiosk.exception.ResourceNotFoundException;
import com.print.kiosk.repository.InventoryItemRepository;
import com.print.kiosk.repository.InventoryTransactionRepository;
import com.print.kiosk.service.interfaces.InventoryTransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class InventoryTransactionServiceImpl
        implements InventoryTransactionService {

    private final InventoryItemRepository itemRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventorySupport inventorySupport;

    public InventoryTransactionServiceImpl(
            InventoryItemRepository itemRepository,
            InventoryTransactionRepository transactionRepository,
            InventorySupport inventorySupport
    ) {
        this.itemRepository = itemRepository;
        this.transactionRepository = transactionRepository;
        this.inventorySupport = inventorySupport;
    }

    @Override
    @Transactional
    public InventoryTransactionResponse recordSale(
            Long itemId,
            Long agentAccountId
    ) {
        return record(
                itemId,
                agentAccountId,
                InventoryTransactionType.SALE,
                1,
                null
        );
    }

    @Override
    @Transactional
    public InventoryTransactionResponse recordReturn(
            Long itemId,
            Long agentAccountId,
            InventoryReasonRequest request
    ) {
        return record(
                itemId,
                agentAccountId,
                InventoryTransactionType.RETURN,
                -1,
                request.getReason()
        );
    }

    @Override
    @Transactional
    public InventoryTransactionResponse recordCorrection(
            Long itemId,
            Long agentAccountId,
            ManualInventoryCorrectionRequest request
    ) {
        if (request.getQuantity() == null || request.getQuantity() == 0) {
            throw new IllegalArgumentException(
                    "Correction quantity must be -1 or 1"
            );
        }

        return record(
                itemId,
                agentAccountId,
                InventoryTransactionType.MANUAL_CORRECTION,
                request.getQuantity(),
                request.getReason()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryTransactionResponse> search(
            Long itemId,
            Long categoryId,
            InventoryTransactionType transactionType,
            Long agentAccountId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        LocalDateTime start = from == null ? null : from.atStartOfDay();
        LocalDateTime end = to == null ? null : to.plusDays(1).atStartOfDay();

        if (start != null && end != null && !start.isBefore(end)) {
            throw new IllegalArgumentException(
                    "The start date must not be after the end date"
            );
        }

        return transactionRepository
                .search(
                        itemId,
                        categoryId,
                        transactionType,
                        agentAccountId,
                        start,
                        end,
                        pageable
                )
                .map(this::toResponse);
    }

    private InventoryTransactionResponse record(
            Long itemId,
            Long agentAccountId,
            InventoryTransactionType transactionType,
            int quantity,
            String reason
    ) {
        UserAccount agent = inventorySupport.approvedAgent(agentAccountId);
        InventoryItem item = itemRepository.findByIdForUpdate(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory item was not found"
                ));

        ensureSaleable(item);
        validateReason(transactionType, reason);

        Long currentQuantityValue = transactionRepository.sumQuantityByItemId(itemId);
        long currentQuantity = currentQuantityValue == null ? 0 : currentQuantityValue;
        long resultingQuantity = currentQuantity + quantity;

        if (resultingQuantity < 0) {
            throw new InventoryConflictException(
                    "This action would make the item's valid net sale quantity negative"
            );
        }

        BigDecimal unitPrice = item.getCurrentPrice().setScale(
                2,
                RoundingMode.HALF_UP
        );
        BigDecimal totalAmount = unitPrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);

        InventoryCategory category = item.getCategory();
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setItem(item);
        transaction.setItemNameSnapshot(item.getName());
        transaction.setCategoryIdSnapshot(category.getId());
        transaction.setCategoryNameSnapshot(category.getName());
        transaction.setQuantity(quantity);
        transaction.setUnitPrice(unitPrice);
        transaction.setTotalAmount(totalAmount);
        transaction.setTransactionType(transactionType);
        transaction.setAgent(agent);
        transaction.setReason(
                reason == null || reason.isBlank()
                        ? null
                        : reason.trim()
        );

        return toResponse(transactionRepository.saveAndFlush(transaction));
    }

    private void ensureSaleable(InventoryItem item) {
        InventoryCategory category = item.getCategory();
        if (item.isArchived()) {
            throw new InventoryConflictException(
                    "Archived inventory items cannot receive transactions"
            );
        }
        if (category.isArchived() || category.isDeleted()) {
            throw new InventoryConflictException(
                    "Items inside an archived or deleted category cannot receive transactions"
            );
        }
    }

    private void validateReason(
            InventoryTransactionType transactionType,
            String reason
    ) {
        if (transactionType != InventoryTransactionType.SALE
                && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException(
                    "A reason is required for returns and manual corrections"
            );
        }
    }

    private InventoryTransactionResponse toResponse(
            InventoryTransaction transaction
    ) {
        UserAccount agent = transaction.getAgent();
        return new InventoryTransactionResponse(
                transaction.getId(),
                transaction.getItem().getId(),
                transaction.getItemNameSnapshot(),
                transaction.getCategoryIdSnapshot(),
                transaction.getCategoryNameSnapshot(),
                transaction.getQuantity(),
                transaction.getUnitPrice(),
                transaction.getTotalAmount(),
                transaction.getTransactionType(),
                agent.getId(),
                agent.getAgentId(),
                agent.getFullName(),
                transaction.getReason(),
                transaction.getTransactedAt()
        );
    }
}