package com.api.artezans.authentication.services;

import com.api.artezans.authentication.dtos.AuthRequest;
import com.api.artezans.authentication.dtos.AuthResponse;
import com.api.artezans.config.security.JwtService;
import com.api.artezans.utils.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;


public interface AuthService {

    AuthResponse authenticateAndGetToken(AuthRequest authRequest);

    ApiResponse logout(HttpServletRequest request);

    JwtService.Tokens refreshToken(HttpServletRequest request);

}
