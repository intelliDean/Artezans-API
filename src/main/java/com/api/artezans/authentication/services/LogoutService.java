package com.api.artezans.authentication.services;

import com.api.artezans.config.security.JwtService;
import com.api.artezans.exceptions.ArtezanException;
import com.api.artezans.tokens.service.interfaces.ArtezanTokenService;
import com.api.artezans.utils.ApiResponse;
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
import static com.api.artezans.utils.ArtezanUtils.BEARER;



@Service
@AllArgsConstructor
public class LogoutService {

    private final ArtezanTokenService artezanTokenService;
    private final JwtService jwtService;

    public ApiResponse logout(HttpServletRequest request) {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(header) || !StringUtils.startsWithIgnoreCase(header, BEARER)) {
            throw new ArtezanException("No valid Authorization header provided");
        }
        final String accessToken = header.substring(7);
        if (!jwtService.validateToken(accessToken)) {
            throw new ArtezanException("Token is invalid or already expired");
        }
        artezanTokenService.revokeToken(accessToken);
        SecurityContextHolder.clearContext();
        return apiResponse("User: %s logged out successfully"
                .formatted(jwtService.extractUsername(accessToken)));
    }
}



//@Service
//@AllArgsConstructor
//public class LogoutService {
//
//    private final ArtezanTokenService artezanTokenService;
//    private final ObjectMapper objectMapper;
//    private final JwtService jwtService;
//
//    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
//
//        if (StringUtils.hasText(header) && StringUtils.startsWithIgnoreCase(header, BEARER)) {
//            final String accessToken = header.substring(7);
//            if (jwtService.validateToken(accessToken)) {
//                artezanTokenService.revokeToken(accessToken);
//                SecurityContextHolder.clearContext();
//
//                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                objectMapper.writeValue(
//                        response.getOutputStream(),
//                        apiResponse("User: %s logged out successfully"
//                                .formatted(jwtService.extractUsername(accessToken))));
//            }
//        }
//    }
//}