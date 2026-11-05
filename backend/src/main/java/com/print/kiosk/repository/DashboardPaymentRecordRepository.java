package com.print.kiosk.repository;

import com.print.kiosk.entity.PaymentRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DashboardPaymentRecordRepository
        extends Repository<PaymentRecord, Long> {

    @Query("""
            select coalesce(sum(payment.amount), 0)
            from PaymentRecord payment
            where payment.status = com.print.kiosk.entity.PaymentStatus.PAID
              and payment.completedAt >= :start
              and payment.completedAt < :end
            """)
    BigDecimal sumPaidAmountBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}