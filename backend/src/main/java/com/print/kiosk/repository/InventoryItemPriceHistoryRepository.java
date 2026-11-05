package com.print.kiosk.repository;

import com.print.kiosk.entity.InventoryItemPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryItemPriceHistoryRepository
        extends JpaRepository<InventoryItemPriceHistory, Long> {

    List<InventoryItemPriceHistory> findByItem_IdOrderByChangedAtDesc(Long itemId);
}