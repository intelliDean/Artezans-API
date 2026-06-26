package com.api.artezans.authentication.services;

import com.api.artezans.users.dto.UserMailInfo;
import com.api.artezans.users.dto.UserMapper;
import com.api.artezans.authentication.dtos.AuthRequest;
import com.api.artezans.authentication.dtos.AuthResponse;
import com.api.artezans.config.security.JwtService;
import com.api.artezans.config.utils.SecurityUtils;
import com.api.artezans.exceptions.ArtezanException;
import com.api.artezans.exceptions.UserNotFoundException;
import com.api.artezans.tokens.model.ArtezanToken;
import com.api.artezans.tokens.model.ArtezanVerificationToken;
import com.api.artezans.tokens.service.interfaces.ArtezanTokenService;
import com.api.artezans.tokens.service.interfaces.ArtezanVerificationTokenService;
import com.api.artezans.users.models.User;
import com.api.artezans.users.services.UserService;
import com.api.artezans.utils.ApiResponse;
import com.api.artezans.utils.ArtezanUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.api.artezans.utils.ArtezanUtils.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${frontend.url}")
    private String frontendUrl;

    private final AuthenticationManager authenticationManager;
    private final ArtezanTokenService artezanTokenService;
    private final ArtezanVerificationTokenService artezanVerificationTokenService;
    private final LogoutService logoutService;
    private final ObjectMapper objectMapper;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    private final UserService userService;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    @Transactional(propagation = REQUIRED)
    public AuthResponse authenticateAndGetToken(AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.emailAddress(),
                            authRequest.password()
                    )
            );

            final User user = userService.findUserByEmail(authRequest.emailAddress());

            return switch (user.getAccountState()) {
               case NOT_VERIFIED -> unverifiedUserEmailAddress(user);
               case DEACTIVATED -> AuthResponse.builder().message(DEACTIVATED).build();
               case VERIFIED -> {
                   JwtService.Tokens tokens = jwtService.generateToken(authentication);
                   saveTokenToDatabase(user, tokens);
                   yield AuthResponse.builder()
                           .user(userMapper.toDTO(user))
                           .message("Authentication successful")
                           .accessToken(tokens.accessToken())
                           .refreshToken(tokens.refreshToken())
                           .build();
               }
               default -> AuthResponse.builder().message("Unusual error logging in").build();
           };
        } catch (AuthenticationException | UserNotFoundException e) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    private void saveTokenToDatabase(User user, JwtService.Tokens tokens) {
        ArtezanToken token = ArtezanToken.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .revoked(false)
//                .expiresAt()
                .user(user)
                .build();
        artezanTokenService.saveToken(token);
    }

    private AuthResponse unverifiedUserEmailAddress(User user) {
        ArtezanVerificationToken verificationToken = artezanVerificationTokenService
                .findByEmail(user.getEmailAddress());
        if (verificationToken.isExpired()) {
            updateTokenAndResendVerificationMail(user);
            return AuthResponse.builder()
                    .message(TOKEN_EXPIRED)
                    .build();
        } else {
            return AuthResponse.builder()
                    .message(tokenStillValidMessage(
                            Duration.between(
                                    LocalDateTime.now(),
                                    verificationToken.getExpireAt()
                            ).toMinutes()
                    )).build();

        }
    }

    private void updateTokenAndResendVerificationMail(User user) {

        ArtezanVerificationToken token = artezanVerificationTokenService.findByEmail(user.getEmailAddress());
        String generatedToken = ArtezanUtils.generateToken(12);
        String emailAddress = user.getEmailAddress();
        token.setToken(generatedToken);
        token.setEmailAddress(emailAddress);
        token.setGeneratedAt(LocalDateTime.now());
        token.setExpireAt(LocalDateTime.now().plusHours(24));
        token.setRevoked(false);
        token.setExpired(false);
        artezanVerificationTokenService.saveToken(token);

        String preUrl = "%s/auth/activating".formatted(frontendUrl);

        // this is just to hash the email address, not the password
        String url = ArtezanUtils.buildActionUrl(
                preUrl,
                generatedToken,
                securityUtils.hash(emailAddress));

        userService.sendMail(new UserMailInfo(user), url);
    }

    @Override
    public ApiResponse logout(HttpServletRequest request) {
        return logoutService.logout(request);
    }

    @Override
    public JwtService.Tokens refreshToken(HttpServletRequest request) {
        final String authHeader = request.getHeader(AUTHORIZATION);

        if (!StringUtils.hasText(authHeader) || !StringUtils.startsWithIgnoreCase(authHeader, BEARER)) {
            throw new ArtezanException("No valid Authorization header provided");
        }

        final String refreshToken = authHeader.substring(7);
        if (!jwtService.validateToken(refreshToken)) {
            throw new ArtezanException("Refresh token is invalid or expired");
        }

        final String email = jwtService.extractUsernameFromToken(refreshToken);
        if (!StringUtils.hasText(email)) {
            throw new ArtezanException("Could not extract email from token");
        }

        final User user = userService.findUserByEmail(email);
        final String accessToken = jwtService.accessToken(user);
        final ArtezanToken token = artezanTokenService.getValidTokenByAnyToken(refreshToken)
                .orElseThrow(() -> new ArtezanException("Token could not be found"));
        token.setAccessToken(accessToken);
        artezanTokenService.saveToken(token);
        return new JwtService.Tokens(accessToken, refreshToken);
    }
}





