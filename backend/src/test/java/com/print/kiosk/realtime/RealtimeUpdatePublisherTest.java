package com.print.kiosk.realtime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RealtimeUpdatePublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void publicQueueChangedPublishesQueueInvalidation() {

        RealtimeUpdatePublisher publisher =
                new RealtimeUpdatePublisher(
                        messagingTemplate
                );

        publisher.publicQueueChanged(
                "REQUEST_CREATED"
        );

        ArgumentCaptor<RealtimeEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        RealtimeEvent.class
                );

        verify(messagingTemplate).convertAndSend(
                eq("/topic/public-queue"),
                eventCaptor.capture()
        );

        RealtimeEvent event =
                eventCaptor.getValue();

        assertEquals(
                "REQUEST_CREATED",
                event.eventType()
        );

        assertEquals(
                "QUEUE",
                event.resource()
        );

        assertNotNull(
                event.occurredAt()
        );
    }
}