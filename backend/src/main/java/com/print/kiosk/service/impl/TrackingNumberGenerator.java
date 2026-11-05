package com.print.kiosk.service.impl;

import com.print.kiosk.repository.PrintRequestRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TrackingNumberGenerator {

    private static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm");

    private final PrintRequestRepository printRequestRepository;

    private LocalDate cachedDate;
    private long nextDailySequence;

    public TrackingNumberGenerator(PrintRequestRepository printRequestRepository) {
        this.printRequestRepository = printRequestRepository;
    }

    public synchronized String nextTrackingNumber() {
        ZonedDateTime now = ZonedDateTime.now(DHAKA_ZONE);
        LocalDate today = now.toLocalDate();
        String date = now.format(DATE_FORMAT);
        String time = now.format(TIME_FORMAT);
        String dailyPrefix = "PR-" + date + "-";

        if (!today.equals(cachedDate)) {
            cachedDate = today;
            nextDailySequence = printRequestRepository
                    .countByTrackingNumberStartingWith(dailyPrefix) + 1;
        }

        while (nextDailySequence <= 999_999) {
            long sequence = nextDailySequence++;
            String trackingNumber = dailyPrefix
                    + time
                    + "-"
                    + String.format("%03d", sequence);

            if (!printRequestRepository.existsByTrackingNumber(trackingNumber)) {
                return trackingNumber;
            }
        }

        throw new IllegalStateException("Unable to generate a unique tracking number for today");
    }
}