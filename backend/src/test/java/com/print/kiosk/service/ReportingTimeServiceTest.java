package com.print.kiosk.service;

import com.print.kiosk.entity.ReportingPeriod;
import com.print.kiosk.service.impl.ReportingTimeService;
import com.print.kiosk.service.model.ReportingWindow;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportingTimeServiceTest {

    private final ReportingTimeService service = new ReportingTimeService();

    @Test
    void dailyWindowUsesTheSelectedDhakaCalendarDay() {
        ReportingWindow window = service.window(
                ReportingPeriod.DAILY,
                LocalDate.of(2026, 8, 6)
        );

        assertEquals(
                LocalDateTime.of(2026, 8, 6, 0, 0),
                window.start()
        );
        assertEquals(
                LocalDateTime.of(2026, 8, 7, 0, 0),
                window.endExclusive()
        );
    }

    @Test
    void weeklyWindowStartsOnMonday() {
        ReportingWindow window = service.window(
                ReportingPeriod.WEEKLY,
                LocalDate.of(2026, 8, 6)
        );

        assertEquals(
                LocalDateTime.of(2026, 8, 3, 0, 0),
                window.start()
        );
        assertEquals(
                LocalDateTime.of(2026, 8, 10, 0, 0),
                window.endExclusive()
        );
    }

    @Test
    void monthlyWindowUsesCalendarMonthBoundaries() {
        ReportingWindow window = service.window(
                ReportingPeriod.MONTHLY,
                LocalDate.of(2026, 8, 20)
        );

        assertEquals(
                LocalDateTime.of(2026, 8, 1, 0, 0),
                window.start()
        );
        assertEquals(
                LocalDateTime.of(2026, 9, 1, 0, 0),
                window.endExclusive()
        );
    }
}