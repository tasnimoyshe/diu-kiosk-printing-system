package com.print.kiosk.service.interfaces;

import com.print.kiosk.dto.request.CreateInventoryCategoryRequest;
import com.print.kiosk.dto.request.MergeInventoryCategoryRequest;
import com.print.kiosk.dto.request.RenameInventoryCategoryRequest;
import com.print.kiosk.dto.response.InventoryCategoryResponse;

import java.util.List;

public interface InventoryCategoryService {

    List<InventoryCategoryResponse> list(boolean includeArchived);

    InventoryCategoryResponse create(
            Long agentAccountId,
            CreateInventoryCategoryRequest request
    );

    InventoryCategoryResponse rename(
            Long categoryId,
            Long agentAccountId,
            RenameInventoryCategoryRequest request
    );

    InventoryCategoryResponse merge(
            Long sourceCategoryId,
            Long agentAccountId,
            MergeInventoryCategoryRequest request
    );

    InventoryCategoryResponse archive(Long categoryId, Long agentAccountId);

    InventoryCategoryResponse reactivate(Long categoryId, Long agentAccountId);

    InventoryCategoryResponse delete(Long categoryId, Long agentAccountId);
}