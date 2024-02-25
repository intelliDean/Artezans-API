package com.api.artezans.gateway.authentication;

import com.api.artezans.authentication.dtos.AuthRequest;
import com.api.artezans.authentication.dtos.AuthResponse;
import com.api.artezans.authentication.services.AuthService;
import com.api.artezans.exceptions.TaskHubException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static com.api.artezans.gateway.authentication.AuthUtil.*;


@RestController
@AllArgsConstructor
@Tag(name = "Auth Controller")
@RequestMapping("api/v1/auth")
public class AuthenticationGateway {

   private final AuthService authenticationService;


    @PostMapping("login")
    @Operation(summary = LOGIN_SUMMARY, description = LOGIN_DESCRIPTION, operationId = LOGIN_OP_ID)
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(
                authenticationService.authenticateAndGetToken(authRequest)
        );
    }

    @PostMapping("logout")
    @Operation(summary =LOGOUT_SUMMARY, description = LOGOUT_DESCRIPTION, operationId = LOGOUT_OP_ID)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            authenticationService.logout(request, response);
        } catch (IOException e) {
            throw new TaskHubException();
        }
    }

    @PostMapping("refresh")
    @Operation(summary = REFRESH_SUMMARY, description = REFRESH_DESCRIPTION, operationId = REFRESH_OP_ID)
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            authenticationService.refreshToken(request, response);
        } catch (IOException e) {
            throw new TaskHubException();
        }
    }
}
