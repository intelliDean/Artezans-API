package com.api.artezans.authentication.services;

import com.api.artezans.config.security.JwtService;
import com.api.artezans.tokens.service.interfaces.TaskHubTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

import static com.api.artezans.utils.ApiResponse.apiResponse;
import static com.api.artezans.utils.TaskHubUtils.BEARER;


@Service
@AllArgsConstructor
public class LogoutService {

    private final TaskHubTokenService taskHubTokenService;
    private final JwtService jwtService;

    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) &&
                StringUtils.startsWithIgnoreCase(header, BEARER)) {
            final String accessToken = header.substring(7);
            if (jwtService.validateToken(accessToken)) {
                taskHubTokenService.revokeToken(accessToken);
                SecurityContextHolder.clearContext();

                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(
                        response.getOutputStream(),
                        apiResponse("User: %s logged out successfully"
                                .formatted(jwtService.extractUsername(accessToken))
                        )
                );
            }
        }
    }
}
