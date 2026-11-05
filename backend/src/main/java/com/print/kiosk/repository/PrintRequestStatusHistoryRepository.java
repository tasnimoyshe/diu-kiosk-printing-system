package com.print.kiosk.repository;

import com.print.kiosk.entity.PrintRequestStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrintRequestStatusHistoryRepository
        extends JpaRepository<PrintRequestStatusHistory, Long> {

    List<PrintRequestStatusHistory> findAllByPrintRequestIdOrderByChangedAtAsc(Long printRequestId);
}