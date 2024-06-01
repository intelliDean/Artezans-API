package com.api.artezans.authentication.services;

import com.api.artezans.authentication.dtos.AuthRequest;
import com.api.artezans.authentication.dtos.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


public interface AuthService {

    AuthResponse authenticateAndGetToken(AuthRequest authRequest);

    void logout(HttpServletRequest request, HttpServletResponse response) throws IOException;

    void refreshToken(HttpServletRequest request,HttpServletResponse response ) throws IOException;

}
