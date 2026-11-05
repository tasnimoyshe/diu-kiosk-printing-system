package com.print.kiosk.service.model;

import java.time.LocalDateTime;

public record ReportingWindow(
        LocalDateTime start,
        LocalDateTime endExclusive
) {
}