package com.print.kiosk.service.impl;

import com.print.kiosk.entity.ReportingPeriod;
import com.print.kiosk.service.model.ReportingWindow;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

@Component
public class ReportingTimeService {

    public static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");

    public LocalDate today() {
        return LocalDate.now(DHAKA_ZONE);
    }

    public ReportingWindow window(
            ReportingPeriod period,
            LocalDate referenceDate
    ) {
        LocalDate date = referenceDate == null ? today() : referenceDate;

        return switch (period) {
            case DAILY -> new ReportingWindow(
                    date.atStartOfDay(),
                    date.plusDays(1).atStartOfDay()
            );
            case WEEKLY -> {
                LocalDate monday = date.with(
                        TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
                );
                yield new ReportingWindow(
                        monday.atStartOfDay(),
                        monday.plusWeeks(1).atStartOfDay()
                );
            }
            case MONTHLY -> {
                LocalDate firstDay = date.withDayOfMonth(1);
                yield new ReportingWindow(
                        firstDay.atStartOfDay(),
                        firstDay.plusMonths(1).atStartOfDay()
                );
            }
        };
    }

    public LocalDateTime now() {
        return LocalDateTime.now(DHAKA_ZONE);
    }
}