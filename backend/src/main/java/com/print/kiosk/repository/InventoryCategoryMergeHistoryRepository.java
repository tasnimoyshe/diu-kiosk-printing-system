package com.print.kiosk.repository;

import com.print.kiosk.entity.InventoryCategoryMergeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryCategoryMergeHistoryRepository
        extends JpaRepository<InventoryCategoryMergeHistory, Long> {
}