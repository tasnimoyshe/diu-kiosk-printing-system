package com.print.kiosk.repository;
import com.print.kiosk.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord,Long>{ List<PaymentRecord> findByPrintRequest_IdOrderByCreatedAtDesc(Long requestId); }