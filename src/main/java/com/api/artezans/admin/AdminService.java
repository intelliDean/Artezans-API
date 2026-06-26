package com.api.artezans.admin;

import com.api.artezans.authentication.dtos.AuthRequest;
import com.api.artezans.authentication.dtos.AuthResponse;
import com.api.artezans.authentication.services.AuthService;
import com.api.artezans.exceptions.UserNotAuthorizedException;
import com.api.artezans.users.models.enums.Role;
import com.api.artezans.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AuthService authService;
    private final UserService userService;

    public AuthResponse adminLogin(AuthRequest authRequest) {

        if (userService.findUserDTOByEmail(authRequest.emailAddress())
                .roles()
                .stream()
                .noneMatch(role -> role == Role.ADMIN)) {
            throw new UserNotAuthorizedException("Access denied: user MUST be an ADMIN");
        }

        return authService.authenticateAndGetToken(authRequest);
    }
}