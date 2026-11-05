package com.print.kiosk.realtime;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class RealtimeSubscriptionInterceptor
        implements ChannelInterceptor {

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null
                || accessor.getCommand() != StompCommand.SUBSCRIBE) {
            return message;
        }

        String destination = accessor.getDestination();

        if (destination == null) {
            throw new AccessDeniedException(
                    "Subscription destination is required"
            );
        }

        switch (destination) {
            case "/topic/public-queue",
                 "/topic/pricing" -> {
            }

            case "/topic/agent-queue",
                 "/topic/inventory" ->
                    requireRole(
                            accessor.getUser(),
                            "ROLE_AGENT"
                    );

            case "/topic/admin-dashboard",
                 "/topic/admin-agents" ->
                    requireRole(
                            accessor.getUser(),
                            "ROLE_ADMIN"
                    );

            default -> throw new AccessDeniedException(
                    "Subscription destination is not allowed"
            );
        }

        return message;
    }

    private void requireRole(
            Principal principal,
            String role
    ) {
        if (!(principal instanceof Authentication authentication)
                || !authentication.isAuthenticated()
                || authentication.getAuthorities()
                .stream()
                .noneMatch(
                        authority ->
                                role.equals(authority.getAuthority())
                )) {

            throw new AccessDeniedException(
                    "You are not authorized for this real-time destination"
            );
        }
    }
}