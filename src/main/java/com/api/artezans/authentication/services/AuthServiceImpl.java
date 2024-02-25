package com.api.artezans.authentication.services;

import com.api.artezans.authentication.dtos.AuthRequest;
import com.api.artezans.authentication.dtos.AuthResponse;
import com.api.artezans.config.security.JwtService;
import com.api.artezans.exceptions.TaskHubException;
import com.api.artezans.tokens.model.TaskHubToken;
import com.api.artezans.tokens.model.TaskHubVerificationToken;
import com.api.artezans.tokens.service.interfaces.TaskHubTokenService;
import com.api.artezans.tokens.service.interfaces.TaskHubVerificationTokenService;
import com.api.artezans.users.dto.UserDTO;
import com.api.artezans.users.models.User;
import com.api.artezans.users.models.enums.AccountState;
import com.api.artezans.users.services.UserService;
import com.api.artezans.utils.TaskHubUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static com.api.artezans.utils.TaskHubUtils.BEARER;
import static com.api.artezans.utils.TaskHubUtils.DEACTIVATED;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${frontend_url}")
    private String frontendUrl;

    private final AuthenticationManager authenticationManager;
    private final TaskHubTokenService taskHubTokenService;
    private final TaskHubVerificationTokenService taskHubVerificationTokenService;
    private final LogoutService logoutService;
    private final UserService userService;
    private final JwtService jwtService;


    @Override
    @Transactional(propagation = REQUIRED)
    public AuthResponse authenticateAndGetToken(AuthRequest authRequest) {
        User user = userService.findUserByEmail(authRequest.getEmailAddress());
        if (user.getAccountState().equals(AccountState.NOT_VERIFIED)) {
            unverifiedUserEmailAddress(user);
        } else if (user.getAccountState().equals(AccountState.DEACTIVATED)) {
            return AuthResponse.builder()
                    .message(DEACTIVATED)
                    .build();
        } else if (user.getAccountState().equals(AccountState.VERIFIED)) {
            return login(authRequest, user);
        }
        throw new TaskHubException("Unusual error logging in");
    }


    private AuthResponse login(AuthRequest authRequest, User user) {
        JwtService.Tokens tokens;
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmailAddress(),
                            authRequest.getPassword())
            );
            if (authentication.isAuthenticated()) {
                tokens = jwtService.generateToken(authentication);
                saveTokenToDatabase(user, tokens);
            } else throw new TaskHubException("Authentication failed");
            return AuthResponse.builder()
                    .user(new UserDTO(user))
                    .message("Authentication successful")
                    .accessToken(tokens.accessToken())
                    .refreshToken(tokens.refreshToken())
                    .build();
        } catch (Exception ex) {
            throw new TaskHubException(ex.getMessage());
        }
    }

    private void saveTokenToDatabase(User user, JwtService.Tokens tokens) {
        TaskHubToken taskHubToken = TaskHubToken.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .revoked(false)
                .user(user)
                .build();
        taskHubTokenService.saveToken(taskHubToken);
    }

    private void unverifiedUserEmailAddress(User user) {
        TaskHubVerificationToken verificationToken =
                taskHubVerificationTokenService.findByEmail(user.getEmailAddress());
        if (verificationToken.isExpired()) {
            updateTokenAndResendVerificationMail(user);
            throw new TaskHubException("New verification email has been sent to your email address");
        } else {
            Duration duration = Duration.between(LocalDateTime.now(), verificationToken.getExpireAt());
            throw new TaskHubException("Your verification token is still valid for the next " +
                    duration.toMinutes() + " minute(s). Check your registered email to get your email verified");
        }
    }

    private void updateTokenAndResendVerificationMail(User user) {
        TaskHubVerificationToken token = taskHubVerificationTokenService.findByEmail(user.getEmailAddress());
        String generatedToken = TaskHubUtils.generateToken(12);
        String emailAddress = user.getEmailAddress();
        token.setToken(generatedToken);
        token.setEmailAddress(emailAddress);
        token.setGeneratedAt(LocalDateTime.now());
        token.setExpireAt(LocalDateTime.now().plusHours(24));
        token.setRevoked(false);
        token.setExpired(false);
        taskHubVerificationTokenService.saveToken(token);

        String preUrl = "%s/auth/activating".formatted(frontendUrl);
        String hashedEmail = new BCryptPasswordEncoder().encode(emailAddress);
        String url = TaskHubUtils.getUrl(hashedEmail, generatedToken, preUrl);

        userService.sendMail(user, url);
    }


    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logoutService.logout(request, response);
    }

    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) ||
                !StringUtils.startsWithIgnoreCase(authHeader, BEARER))
            return;
        final String refreshToken = authHeader.substring(7);
        if (jwtService.validateToken(refreshToken)) {
            final String email = jwtService.extractUsername(refreshToken);
            if (StringUtils.hasText(email)) {
                final User user = userService.findUserByEmail(email);
                final String accessToken = jwtService.accessToken(user);
                final TaskHubToken taskHubToken = taskHubTokenService.getValidTokenByAnyToken(refreshToken)
                        .orElseThrow(() -> new TaskHubException("Token could not be found"));
                taskHubToken.setAccessToken(accessToken);
                taskHubTokenService.saveToken(taskHubToken);
                final JwtService.Tokens newTokens = new JwtService.Tokens(accessToken, refreshToken);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), newTokens);
            }
        }
    }

}

