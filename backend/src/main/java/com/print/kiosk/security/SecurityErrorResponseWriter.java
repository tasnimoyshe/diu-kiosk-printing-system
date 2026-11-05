package com.print.kiosk.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Component
public class SecurityErrorResponseWriter {

    private static final ZoneId DHAKA_ZONE =
            ZoneId.of("Asia/Dhaka");

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String error,
            String message
    ) throws IOException {
        String referenceId = UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        String json = "{"
                + "\"timestamp\":\""
                + escape(LocalDateTime.now(DHAKA_ZONE).toString())
                + "\","
                + "\"status\":"
                + status
                + ","
                + "\"error\":\""
                + escape(error)
                + "\","
                + "\"message\":\""
                + escape(message)
                + "\","
                + "\"path\":\""
                + escape(request.getRequestURI())
                + "\","
                + "\"referenceId\":\""
                + escape(referenceId)
                + "\""
                + "}";

        response.setStatus(status);
        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );
        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );
        response.getWriter().write(json);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}