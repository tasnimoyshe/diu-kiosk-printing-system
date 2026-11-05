package com.print.kiosk.service.interfaces;

import com.print.kiosk.dto.request.ChangeInventoryItemPriceRequest;
import com.print.kiosk.dto.request.CreateInventoryItemRequest;
import com.print.kiosk.dto.request.RenameInventoryItemRequest;
import com.print.kiosk.dto.response.InventoryItemPriceHistoryResponse;
import com.print.kiosk.dto.response.InventoryItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryItemService {

    Page<InventoryItemResponse> list(
            Long categoryId,
            String search,
            boolean includeArchived,
            Pageable pageable
    );

    InventoryItemResponse get(Long itemId);

    InventoryItemResponse create(
            Long agentAccountId,
            CreateInventoryItemRequest request
    );

    InventoryItemResponse rename(
            Long itemId,
            Long agentAccountId,
            RenameInventoryItemRequest request
    );

    InventoryItemResponse changePrice(
            Long itemId,
            Long agentAccountId,
            ChangeInventoryItemPriceRequest request
    );

    InventoryItemResponse archive(Long itemId, Long agentAccountId);

    InventoryItemResponse reactivate(Long itemId, Long agentAccountId);

    List<InventoryItemPriceHistoryResponse> priceHistory(Long itemId);
}