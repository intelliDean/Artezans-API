package com.api.artezans.config.utils;


import com.api.artezans.exceptions.TaskHubException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class TaskHubAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> data = new HashMap<>();
        data.put("status", false);
        data.put("message", accessDeniedException.getMessage());

        log.info("Access Denied Exception: {} ", accessDeniedException.getMessage());

        try (PrintWriter writer = response.getWriter()) {
            objectMapper.writeValue(writer, data);
        } catch (IOException e) {
            log.info("IOException message: {}", e.getMessage());
            throw new TaskHubException("Error writing JSON response");
        }
    }
}
