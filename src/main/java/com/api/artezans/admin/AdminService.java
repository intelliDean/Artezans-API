package com.api.artezans.admin;

import com.api.artezans.authentication.dtos.AuthRequest;
import com.api.artezans.authentication.dtos.AuthResponse;
import com.api.artezans.authentication.services.AuthService;
import com.api.artezans.exceptions.UserNotAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AdminService {
    private final AuthService authService;

    public AuthResponse adminLogin(AuthRequest authRequest) {
        final String adminEmail = "info@taskhub.com";

        if (authRequest.getEmailAddress().equals(adminEmail)) {
            return authService.authenticateAndGetToken(authRequest);
        }
        throw new UserNotAuthorizedException();
    }
}