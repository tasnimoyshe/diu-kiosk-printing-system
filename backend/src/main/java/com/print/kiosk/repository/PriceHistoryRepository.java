package com.print.kiosk.repository;

import com.print.kiosk.entity.PriceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    Page<PriceHistory> findAllByOrderByChangedAtDesc(Pageable pageable);
}