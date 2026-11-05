package com.print.kiosk.repository;

import com.print.kiosk.entity.PrintingPrice;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PrintingPriceRepository extends JpaRepository<PrintingPrice, Long> {

    Optional<PrintingPrice> findByConfigKey(String configKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select price from PrintingPrice price where price.configKey = :configKey")
    Optional<PrintingPrice> findByConfigKeyForUpdate(@Param("configKey") String configKey);
}