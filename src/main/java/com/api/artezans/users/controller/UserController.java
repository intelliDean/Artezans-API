package com.api.artezans.users.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import task.hub.exceptions.TaskHubException;
import task.hub.exceptions.TokenException;
import task.hub.user.password.dtos.EmailParam;
import task.hub.user.password.dtos.ResetPasswordRequest;
import task.hub.user.users.models.User;
import task.hub.user.users.services.UserService;
import task.hub.user.utils.ApiResponse;
import task.hub.user.utils.TaskHubUtils;

import java.util.Optional;

import static task.hub.user.utils.ApiResponse.apiResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserController {

    @Value("${frontend_url}")
    private String frontendUrl;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public ApiResponse createLinkForPasswordRequest(EmailParam emailParam) {
        Optional<User> user = Optional.ofNullable(userService.findUserByEmail(emailParam.getEmail()));
        String passwordResetUrl = "";
        if (user.isPresent()) {
            String passwordToken = TaskHubUtils.generateToken(12);
            userService.savePasswordResetToken(user, passwordToken);
            sendResetUrl(user.get(), passwordToken);
        }
        return apiResponse("Password reset email has been sent to your mail");
    }

    public ApiResponse resetPassword(ResetPasswordRequest passwordResetRequest, String token) {
        String tokenValidation = userService.verifyPasswordResetToken(token);
        if (!tokenValidation.equalsIgnoreCase("valid")) {
            throw new TokenException("Invalid password reset token");
        }
        User user = userService.findUserByPasswordToken(token);
        if (user != null) {
            resetUserPassword(user, passwordResetRequest.getPassword());
            return apiResponse("Password reset successful");
        } else {
            throw new TaskHubException("Invalid password reset token");
        }
    }

    private void sendResetUrl(User user, String token) {
        String url = "%s/auth/reset-password?t=%s".formatted(frontendUrl, token);
       // String url = String.format("%s/auth/reset-password?t=%s", frontendUrl, token);
        try {
            userService.sendPasswordResetMail(user, url);
        } catch (Exception e) {
            throw new TaskHubException("Error sending change of password email");
        }
    }

    private void resetUserPassword(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        userService.saveUser(user);
    }

    public ApiResponse verifyUserEmail(String token, String email) {
        return userService.verified(token, email);
    }

    public ApiResponse deactivateAccount() {
        return userService.deactivateAccount();
    }

    public ApiResponse reactivate(String email, String token) {
        return userService.reactivate(email, token);
    }

    public ApiResponse sendActivationMail(String emailAddress, HttpServletRequest request) {
        return userService.sendActivationMail(emailAddress, request);
    }

}
