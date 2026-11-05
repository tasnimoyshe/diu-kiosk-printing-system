package com.print.kiosk.realtime;

import java.time.LocalDateTime;

public record RealtimeEvent(
        String eventType,
        String resource,
        LocalDateTime occurredAt
) {
}