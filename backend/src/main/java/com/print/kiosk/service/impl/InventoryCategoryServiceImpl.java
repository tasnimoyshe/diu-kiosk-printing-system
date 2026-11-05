package com.print.kiosk.service.impl;

import com.print.kiosk.dto.request.CreateInventoryCategoryRequest;
import com.print.kiosk.dto.request.MergeInventoryCategoryRequest;
import com.print.kiosk.dto.request.RenameInventoryCategoryRequest;
import com.print.kiosk.dto.response.InventoryCategoryResponse;
import com.print.kiosk.entity.InventoryCategory;
import com.print.kiosk.entity.InventoryCategoryMergeHistory;
import com.print.kiosk.entity.InventoryItem;
import com.print.kiosk.entity.UserAccount;
import com.print.kiosk.exception.InventoryConflictException;
import com.print.kiosk.exception.ResourceNotFoundException;
import com.print.kiosk.repository.InventoryCategoryMergeHistoryRepository;
import com.print.kiosk.repository.InventoryCategoryRepository;
import com.print.kiosk.repository.InventoryItemRepository;
import com.print.kiosk.service.interfaces.InventoryCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class InventoryCategoryServiceImpl
        implements InventoryCategoryService {

    private static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");

    private final InventoryCategoryRepository categoryRepository;
    private final InventoryItemRepository itemRepository;
    private final InventoryCategoryMergeHistoryRepository mergeHistoryRepository;
    private final InventorySupport inventorySupport;

    public InventoryCategoryServiceImpl(
            InventoryCategoryRepository categoryRepository,
            InventoryItemRepository itemRepository,
            InventoryCategoryMergeHistoryRepository mergeHistoryRepository,
            InventorySupport inventorySupport
    ) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.mergeHistoryRepository = mergeHistoryRepository;
        this.inventorySupport = inventorySupport;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryCategoryResponse> list(boolean includeArchived) {
        List<InventoryCategory> categories = includeArchived
                ? categoryRepository.findAllByDeletedFalseOrderByArchivedAscNameAsc()
                : categoryRepository.findAllByDeletedFalseAndArchivedFalseOrderByNameAsc();

        return categories.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public InventoryCategoryResponse create(
            Long agentAccountId,
            CreateInventoryCategoryRequest request
    ) {
        inventorySupport.approvedAgent(agentAccountId);

        String name = inventorySupport.cleanName(request.getName(), "Category name");
        String normalizedName = inventorySupport.normalizeName(name);

        if (categoryRepository.existsByNormalizedName(normalizedName)) {
            throw new InventoryConflictException(
                    "An inventory category with this name already exists"
            );
        }

        InventoryCategory category = new InventoryCategory();
        category.setName(name);
        category.setNormalizedName(normalizedName);
        category.setArchived(false);
        category.setDeleted(false);

        return toResponse(categoryRepository.saveAndFlush(category));
    }

    @Override
    @Transactional
    public InventoryCategoryResponse rename(
            Long categoryId,
            Long agentAccountId,
            RenameInventoryCategoryRequest request
    ) {
        inventorySupport.approvedAgent(agentAccountId);
        InventoryCategory category = activeCategoryForUpdate(categoryId);

        String name = inventorySupport.cleanName(request.getName(), "Category name");
        String normalizedName = inventorySupport.normalizeName(name);

        if (categoryRepository.existsByNormalizedNameAndDeletedFalseAndIdNot(
                normalizedName,
                categoryId
        )) {
            throw new InventoryConflictException(
                    "An inventory category with this name already exists"
            );
        }

        category.setName(name);
        category.setNormalizedName(normalizedName);

        return toResponse(categoryRepository.saveAndFlush(category));
    }

    @Override
    @Transactional
    public InventoryCategoryResponse merge(
            Long sourceCategoryId,
            Long agentAccountId,
            MergeInventoryCategoryRequest request
    ) {
        UserAccount agent = inventorySupport.approvedAgent(agentAccountId);
        Long destinationCategoryId = request.getDestinationCategoryId();

        if (sourceCategoryId.equals(destinationCategoryId)) {
            throw new IllegalArgumentException(
                    "Source and destination categories must be different"
            );
        }

        Long firstId = Math.min(sourceCategoryId, destinationCategoryId);
        Long secondId = Math.max(sourceCategoryId, destinationCategoryId);

        InventoryCategory first = categoryRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory category was not found"
                ));
        InventoryCategory second = categoryRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory category was not found"
                ));

        InventoryCategory source = first.getId().equals(sourceCategoryId)
                ? first
                : second;
        InventoryCategory destination = first.getId().equals(destinationCategoryId)
                ? first
                : second;

        validateUsableForMerge(source, "Source");
        validateUsableForMerge(destination, "Destination");

        if (destination.isArchived()) {
            throw new InventoryConflictException(
                    "The destination category must be active"
            );
        }

        List<InventoryItem> items = itemRepository
                .findAllByCategory_IdOrderByNameAsc(source.getId());

        for (InventoryItem item : items) {
            if (itemRepository.existsByCategory_IdAndNormalizedName(
                    destination.getId(),
                    item.getNormalizedName()
            )) {
                throw new InventoryConflictException(
                        "Cannot merge categories because the destination already contains an item named '"
                                + item.getName()
                                + "'"
                );
            }
        }

        for (InventoryItem item : items) {
            item.setCategory(destination);
        }
        itemRepository.saveAll(items);

        String sourceName = source.getName();
        LocalDateTime now = LocalDateTime.now(DHAKA_ZONE);
        source.setArchived(true);
        source.setMergedIntoCategory(destination);
        source.setMergedAt(now);
        source.setMergedByAgent(agent);
        categoryRepository.save(source);

        InventoryCategoryMergeHistory mergeHistory =
                new InventoryCategoryMergeHistory();
        mergeHistory.setSourceCategory(source);
        mergeHistory.setSourceCategoryName(sourceName);
        mergeHistory.setDestinationCategory(destination);
        mergeHistory.setDestinationCategoryName(destination.getName());
        mergeHistory.setMovedItemCount(items.size());
        mergeHistory.setMergedByAgent(agent);
        mergeHistoryRepository.save(mergeHistory);

        return toResponse(destination);
    }

    @Override
    @Transactional
    public InventoryCategoryResponse archive(
            Long categoryId,
            Long agentAccountId
    ) {
        inventorySupport.approvedAgent(agentAccountId);
        InventoryCategory category = activeCategoryForUpdate(categoryId);

        if (category.isArchived()) {
            return toResponse(category);
        }

        category.setArchived(true);
        List<InventoryItem> items = itemRepository
                .findAllByCategory_IdOrderByNameAsc(categoryId);
        items.forEach(item -> item.setArchived(true));
        itemRepository.saveAll(items);

        return toResponse(categoryRepository.saveAndFlush(category));
    }

    @Override
    @Transactional
    public InventoryCategoryResponse reactivate(
            Long categoryId,
            Long agentAccountId
    ) {
        inventorySupport.approvedAgent(agentAccountId);
        InventoryCategory category = categoryRepository.findByIdForUpdate(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory category was not found"
                ));

        if (category.isDeleted()) {
            throw new InventoryConflictException(
                    "A deleted category cannot be reactivated"
            );
        }
        if (category.getMergedIntoCategory() != null) {
            throw new InventoryConflictException(
                    "A merged category cannot be reactivated"
            );
        }

        category.setArchived(false);
        return toResponse(categoryRepository.saveAndFlush(category));
    }

    @Override
    @Transactional
    public InventoryCategoryResponse delete(
            Long categoryId,
            Long agentAccountId
    ) {
        inventorySupport.approvedAgent(agentAccountId);
        InventoryCategory category = activeCategoryForUpdate(categoryId);

        long activeItems = itemRepository.countByCategory_IdAndArchivedFalse(categoryId);
        if (activeItems > 0) {
            throw new InventoryConflictException(
                    "A category can be deleted only when it has no active items"
            );
        }

        category.setArchived(true);
        category.setDeleted(true);
        return toResponse(categoryRepository.saveAndFlush(category));
    }

    private InventoryCategory activeCategoryForUpdate(Long categoryId) {
        InventoryCategory category = categoryRepository.findByIdForUpdate(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory category was not found"
                ));

        if (category.isDeleted()) {
            throw new ResourceNotFoundException(
                    "Inventory category was not found"
            );
        }
        return category;
    }

    private void validateUsableForMerge(
            InventoryCategory category,
            String label
    ) {
        if (category.isDeleted()) {
            throw new ResourceNotFoundException(
                    label + " inventory category was not found"
            );
        }
        if (category.getMergedIntoCategory() != null) {
            throw new InventoryConflictException(
                    label + " category has already been merged"
            );
        }
    }

    private InventoryCategoryResponse toResponse(InventoryCategory category) {
        InventoryCategory mergedInto = category.getMergedIntoCategory();
        return new InventoryCategoryResponse(
                category.getId(),
                category.getName(),
                category.isArchived(),
                category.isDeleted(),
                mergedInto == null ? null : mergedInto.getId(),
                mergedInto == null ? null : mergedInto.getName(),
                itemRepository.countByCategory_IdAndArchivedFalse(category.getId()),
                itemRepository.countByCategory_Id(category.getId()),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}