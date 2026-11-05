package com.print.kiosk.realtime;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class RealtimeSubscriptionInterceptorTest {

    private final RealtimeSubscriptionInterceptor interceptor =
            new RealtimeSubscriptionInterceptor();

    private final MessageChannel channel =
            mock(MessageChannel.class);

    @Test
    void publicTopicDoesNotRequireAuthentication() {

        Message<byte[]> message =
                subscription(
                        "/topic/public-queue",
                        null
                );

        assertDoesNotThrow(
                () -> interceptor.preSend(
                        message,
                        channel
                )
        );
    }

    @Test
    void agentCanSubscribeToAgentTopic() {

        Message<byte[]> message =
                subscription(
                        "/topic/agent-queue",
                        authentication("ROLE_AGENT")
                );

        assertDoesNotThrow(
                () -> interceptor.preSend(
                        message,
                        channel
                )
        );
    }

    @Test
    void agentCannotSubscribeToAdminTopic() {

        Message<byte[]> message =
                subscription(
                        "/topic/admin-dashboard",
                        authentication("ROLE_AGENT")
                );

        assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(
                        message,
                        channel
                )
        );
    }

    private Message<byte[]> subscription(
            String destination,
            Authentication authentication
    ) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(
                        StompCommand.SUBSCRIBE
                );

        accessor.setDestination(destination);

        if (authentication != null) {
            accessor.setUser(authentication);
        }

        return MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );
    }

    private Authentication authentication(
            String role
    ) {
        return new UsernamePasswordAuthenticationToken(
                "test-user",
                "not-used",
                List.of(
                        new SimpleGrantedAuthority(role)
                )
        );
    }
}