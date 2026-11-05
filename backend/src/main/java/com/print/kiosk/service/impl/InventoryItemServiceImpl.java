package com.print.kiosk.service.impl;

import com.print.kiosk.dto.request.ChangeInventoryItemPriceRequest;
import com.print.kiosk.dto.request.CreateInventoryItemRequest;
import com.print.kiosk.dto.request.RenameInventoryItemRequest;
import com.print.kiosk.dto.response.InventoryItemPriceHistoryResponse;
import com.print.kiosk.dto.response.InventoryItemResponse;
import com.print.kiosk.entity.InventoryCategory;
import com.print.kiosk.entity.InventoryItem;
import com.print.kiosk.entity.InventoryItemPriceHistory;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.exception.InventoryConflictException;
import com.print.kiosk.exception.ResourceNotFoundException;
import com.print.kiosk.repository.InventoryCategoryRepository;
import com.print.kiosk.repository.InventoryItemPriceHistoryRepository;
import com.print.kiosk.repository.InventoryItemRepository;
import com.print.kiosk.repository.InventoryTransactionRepository;
import com.print.kiosk.service.interfaces.InventoryItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryItemServiceImpl implements InventoryItemService {

    private final InventoryItemRepository itemRepository;
    private final InventoryCategoryRepository categoryRepository;
    private final InventoryItemPriceHistoryRepository priceHistoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventorySupport inventorySupport;

    public InventoryItemServiceImpl(
            InventoryItemRepository itemRepository,
            InventoryCategoryRepository categoryRepository,
            InventoryItemPriceHistoryRepository priceHistoryRepository,
            InventoryTransactionRepository transactionRepository,
            InventorySupport inventorySupport
    ) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.transactionRepository = transactionRepository;
        this.inventorySupport = inventorySupport;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryItemResponse> list(
            Long categoryId,
            String search,
            boolean includeArchived,
            Pageable pageable
    ) {
        String normalizedSearch = search == null || search.isBlank()
                ? null
                : search.trim();

        return itemRepository
                .search(categoryId, normalizedSearch, includeArchived, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItemResponse get(Long itemId) {
        return toResponse(detailedItem(itemId));
    }

    @Override
    @Transactional
    public InventoryItemResponse create(
            Long agentAccountId,
            CreateInventoryItemRequest request
    ) {
        UserAccount agent = inventorySupport.approvedAgent(agentAccountId);
        InventoryCategory category = resolveCategory(request);
        ensureCategoryAvailable(category);

        String name = inventorySupport.cleanName(request.getName(), "Item name");
        String normalizedName = inventorySupport.normalizeName(name);

        if (itemRepository.existsByCategory_IdAndNormalizedName(
                category.getId(),
                normalizedName
        )) {
            throw new InventoryConflictException(
                    "An item with this name already exists in the selected category"
            );
        }

        InventoryItem item = new InventoryItem();
        item.setName(name);
        item.setNormalizedName(normalizedName);
        item.setCategory(category);
        item.setCurrentPrice(inventorySupport.money(request.getPrice()));
        item.setArchived(false);
        item.setCreatedByAgent(agent);

        return toResponse(itemRepository.saveAndFlush(item));
    }

    @Override
    @Transactional
    public InventoryItemResponse rename(
            Long itemId,
            Long agentAccountId,
            RenameInventoryItemRequest request
    ) {
        inventorySupport.approvedAgent(agentAccountId);
        InventoryItem item = itemForUpdate(itemId);

        String name = inventorySupport.cleanName(request.getName(), "Item name");
        String normalizedName = inventorySupport.normalizeName(name);

        if (itemRepository.existsByCategory_IdAndNormalizedNameAndIdNot(
                item.getCategory().getId(),
                normalizedName,
                itemId
        )) {
            throw new InventoryConflictException(
                    "An item with this name already exists in the selected category"
            );
        }

        item.setName(name);
        item.setNormalizedName(normalizedName);
        return toResponse(itemRepository.saveAndFlush(item));
    }

    @Override
    @Transactional
    public InventoryItemResponse changePrice(
            Long itemId,
            Long agentAccountId,
            ChangeInventoryItemPriceRequest request
    ) {
        UserAccount agent = inventorySupport.approvedAgent(agentAccountId);
        InventoryItem item = itemForUpdate(itemId);
        BigDecimal newPrice = inventorySupport.money(request.getPrice());
        BigDecimal previousPrice = item.getCurrentPrice().setScale(2);

        if (previousPrice.compareTo(newPrice) == 0) {
            return toResponse(item);
        }

        item.setCurrentPrice(newPrice);
        itemRepository.saveAndFlush(item);

        InventoryItemPriceHistory history = new InventoryItemPriceHistory();
        history.setItem(item);
        history.setPreviousPrice(previousPrice);
        history.setNewPrice(newPrice);
        history.setChangedByAgent(agent);
        priceHistoryRepository.save(history);

        return toResponse(item);
    }

    @Override
    @Transactional
    public InventoryItemResponse archive(Long itemId, Long agentAccountId) {
        inventorySupport.approvedAgent(agentAccountId);
        InventoryItem item = itemForUpdate(itemId);
        item.setArchived(true);
        return toResponse(itemRepository.saveAndFlush(item));
    }

    @Override
    @Transactional
    public InventoryItemResponse reactivate(Long itemId, Long agentAccountId) {
        inventorySupport.approvedAgent(agentAccountId);
        InventoryItem item = itemForUpdate(itemId);
        ensureCategoryAvailable(item.getCategory());
        item.setArchived(false);
        return toResponse(itemRepository.saveAndFlush(item));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemPriceHistoryResponse> priceHistory(Long itemId) {
        detailedItem(itemId);
        return priceHistoryRepository
                .findByItem_IdOrderByChangedAtDesc(itemId)
                .stream()
                .map(history -> new InventoryItemPriceHistoryResponse(
                        history.getId(),
                        history.getItem().getId(),
                        history.getPreviousPrice(),
                        history.getNewPrice(),
                        history.getChangedByAgent().getId(),
                        history.getChangedByAgent().getAgentId(),
                        history.getChangedAt()
                ))
                .toList();
    }

    private InventoryCategory resolveCategory(CreateInventoryItemRequest request) {
        boolean hasId = request.getCategoryId() != null;
        boolean hasName = request.getCategoryName() != null
                && !request.getCategoryName().isBlank();

        if (hasId == hasName) {
            throw new IllegalArgumentException(
                    "Provide either categoryId or categoryName, but not both"
            );
        }

        if (hasId) {
            return categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory category was not found"
                    ));
        }

        String categoryName = inventorySupport.cleanName(
                request.getCategoryName(),
                "Category name"
        );
        String normalizedName = inventorySupport.normalizeName(categoryName);

        return categoryRepository
                .findByNormalizedName(normalizedName)
                .map(category -> {
                    if (category.isDeleted()) {
                        throw new InventoryConflictException(
                                "A deleted category already reserves this name"
                        );
                    }
                    return category;
                })
                .orElseGet(() -> {
                    InventoryCategory category = new InventoryCategory();
                    category.setName(categoryName);
                    category.setNormalizedName(normalizedName);
                    category.setArchived(false);
                    category.setDeleted(false);
                    return categoryRepository.saveAndFlush(category);
                });
    }

    private InventoryItem detailedItem(Long itemId) {
        return itemRepository.findDetailedById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory item was not found"
                ));
    }

    private InventoryItem itemForUpdate(Long itemId) {
        return itemRepository.findByIdForUpdate(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory item was not found"
                ));
    }

    private void ensureCategoryAvailable(InventoryCategory category) {
        if (category.isDeleted()) {
            throw new ResourceNotFoundException(
                    "Inventory category was not found"
            );
        }
        if (category.isArchived()) {
            throw new InventoryConflictException(
                    "Inventory items cannot be created or reactivated inside an archived category"
            );
        }
        if (category.getMergedIntoCategory() != null) {
            throw new InventoryConflictException(
                    "This category has been merged into another category"
            );
        }
    }

    private InventoryItemResponse toResponse(InventoryItem item) {
        Long netQuantity = transactionRepository.sumQuantityByItemId(item.getId());
        BigDecimal netRevenue = transactionRepository.sumAmountByItemId(item.getId());
        UserAccount creator = item.getCreatedByAgent();

        return new InventoryItemResponse(
                item.getId(),
                item.getName(),
                item.getCategory().getId(),
                item.getCategory().getName(),
                item.getCurrentPrice(),
                item.isArchived(),
                creator == null ? null : creator.getId(),
                creator == null ? null : creator.getAgentId(),
                netQuantity == null ? 0 : netQuantity,
                netRevenue == null ? BigDecimal.ZERO.setScale(2) : netRevenue,
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}