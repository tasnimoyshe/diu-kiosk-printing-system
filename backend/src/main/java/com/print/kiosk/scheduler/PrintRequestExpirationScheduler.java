package com.print.kiosk.scheduler;

import com.print.kiosk.realtime.RealtimeUpdatePublisher;
import com.print.kiosk.service.interfaces.PrintRequestExpirationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PrintRequestExpirationScheduler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    PrintRequestExpirationScheduler.class
            );

    private final PrintRequestExpirationService expirationService;
    private final RealtimeUpdatePublisher realtimeUpdatePublisher;

    public PrintRequestExpirationScheduler(
            PrintRequestExpirationService expirationService,
            RealtimeUpdatePublisher realtimeUpdatePublisher
    ) {
        this.expirationService = expirationService;
        this.realtimeUpdatePublisher = realtimeUpdatePublisher;
    }

    @Scheduled(
            fixedDelayString =
                    "${app.print-request.expiration-scan-ms:60000}"
    )
    public void expireUnpaidRequests() {

        int expiredCount =
                expirationService.expireDueRequests();

        if (expiredCount > 0) {
            LOGGER.info(
                    "Expired {} unpaid print request(s).",
                    expiredCount
            );

            realtimeUpdatePublisher.publicQueueChanged(
                    "REQUEST_EXPIRED"
            );

            realtimeUpdatePublisher.agentQueueChanged(
                    "REQUEST_EXPIRED"
            );

            realtimeUpdatePublisher.adminDashboardChanged(
                    "REQUEST_EXPIRED"
            );
        }
    }
}