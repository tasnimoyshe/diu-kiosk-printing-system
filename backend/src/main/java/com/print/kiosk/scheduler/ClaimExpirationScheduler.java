package com.print.kiosk.scheduler;

import com.print.kiosk.realtime.RealtimeUpdatePublisher;
import com.print.kiosk.service.interfaces.ClaimExpirationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ClaimExpirationScheduler {

    private final ClaimExpirationService service;
    private final RealtimeUpdatePublisher realtimeUpdatePublisher;

    public ClaimExpirationScheduler(
            ClaimExpirationService service,
            RealtimeUpdatePublisher realtimeUpdatePublisher
    ) {
        this.service = service;
        this.realtimeUpdatePublisher = realtimeUpdatePublisher;
    }

    @Scheduled(
            fixedDelayString =
                    "${app.queue.claim-expiration-scan-ms:60000}"
    )
    public void run() {

        int released =
                service.releaseExpiredClaims();

        if (released > 0) {
            realtimeUpdatePublisher.publicQueueChanged(
                    "CLAIM_EXPIRED"
            );

            realtimeUpdatePublisher.agentQueueChanged(
                    "CLAIM_EXPIRED"
            );

            realtimeUpdatePublisher.adminDashboardChanged(
                    "CLAIM_EXPIRED"
            );
        }
    }
}