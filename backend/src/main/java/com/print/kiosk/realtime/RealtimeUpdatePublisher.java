package com.print.kiosk.realtime;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class RealtimeUpdatePublisher {

    private static final ZoneId DHAKA =
            ZoneId.of("Asia/Dhaka");

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeUpdatePublisher(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publicQueueChanged(String eventType) {
        send(
                "/topic/public-queue",
                eventType,
                "QUEUE"
        );
    }

    public void agentQueueChanged(String eventType) {
        send(
                "/topic/agent-queue",
                eventType,
                "QUEUE"
        );
    }

    public void adminDashboardChanged(String eventType) {
        send(
                "/topic/admin-dashboard",
                eventType,
                "DASHBOARD"
        );
    }

    public void adminAgentsChanged(String eventType) {
        send(
                "/topic/admin-agents",
                eventType,
                "AGENTS"
        );
    }

    public void pricingChanged(String eventType) {
        send(
                "/topic/pricing",
                eventType,
                "PRICING"
        );
    }

    public void inventoryChanged(String eventType) {
        send(
                "/topic/inventory",
                eventType,
                "INVENTORY"
        );
    }

    private void send(
            String destination,
            String eventType,
            String resource
    ) {
        messagingTemplate.convertAndSend(
                destination,
                new RealtimeEvent(
                        eventType,
                        resource,
                        LocalDateTime.now(DHAKA)
                )
        );
    }
}