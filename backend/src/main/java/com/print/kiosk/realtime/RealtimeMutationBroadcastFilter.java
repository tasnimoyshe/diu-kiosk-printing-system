package com.print.kiosk.realtime;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class RealtimeMutationBroadcastFilter extends OncePerRequestFilter {

    private static final Set<String> MUTATING_METHODS =
            Set.of(
                    "POST",
                    "PUT",
                    "PATCH",
                    "DELETE"
            );

    private final RealtimeUpdatePublisher publisher;

    public RealtimeMutationBroadcastFilter(
            RealtimeUpdatePublisher publisher
    ) {
        this.publisher = publisher;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        filterChain.doFilter(request, response);

        if (response.getStatus() < 200
                || response.getStatus() >= 300) {
            return;
        }

        String method = request.getMethod();
        String path = request.getRequestURI();

        boolean fileDownloadChangedState =
                "GET".equals(method)
                        && path.matches(
                        "^/api/agent/requests/\\d+/files/\\d+/download$"
                );

        if (!MUTATING_METHODS.contains(method)
                && !fileDownloadChangedState) {
            return;
        }

        publish(
                path,
                method + "_SUCCESS"
        );
    }

    private void publish(
            String path,
            String eventType
    ) {
        if ("/api/print-requests/preview".equals(path)) {
            return;
        }

        if ("/api/print-requests".equals(path)
                || path.startsWith("/api/print-requests/")) {

            publishQueue(eventType);
            return;
        }

        if (path.startsWith("/api/agent/queue")
                || path.startsWith("/api/agent/requests")
                || path.startsWith("/api/admin/requests")) {

            publishQueue(eventType);
            return;
        }

        if (path.startsWith("/api/agent/pricing")) {
            publisher.pricingChanged(eventType);
            return;
        }

        if (path.startsWith("/api/agent/inventory")) {
            publisher.inventoryChanged(eventType);
            publisher.adminDashboardChanged(eventType);
            return;
        }

        if ("/api/auth/agents/register".equals(path)
                || path.startsWith("/api/admin/agents")) {

            publisher.adminAgentsChanged(eventType);
        }
    }

    private void publishQueue(
            String eventType
    ) {
        publisher.publicQueueChanged(eventType);
        publisher.agentQueueChanged(eventType);
        publisher.adminDashboardChanged(eventType);
    }
}