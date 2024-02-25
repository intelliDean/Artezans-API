package com.api.artezans.config.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component
public class TaskHubAuthEntryPoint implements AuthenticationEntryPoint {
    private static final int UNAUTHORIZED_STATUS_CODE = HttpServletResponse.SC_UNAUTHORIZED;
    private static final String APPLICATION_JSON_CONTENT_TYPE = MediaType.APPLICATION_JSON_VALUE;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        log.error("Unauthorized: {}", authException.getMessage());

        if (!response.isCommitted()) {
            response.setStatus(UNAUTHORIZED_STATUS_CODE);
            response.setContentType(APPLICATION_JSON_CONTENT_TYPE);

            try (PrintWriter writer = response.getWriter()) {
                writer.write(generateErrorMessage(authException));
            }
        } else {
            log.warn("Unauthorized request received, but response has already been committed.");
        }
    }


    private String generateErrorMessage(AuthenticationException authException) {
        return "{\"Unauthorized\" : \"%s\"}".formatted(authException.getMessage());
    }
}