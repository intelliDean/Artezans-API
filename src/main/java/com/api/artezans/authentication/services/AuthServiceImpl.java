package com.api.artezans.authentication.services;

import com.api.artezans.users.dto.UserMapper;
import com.api.artezans.authentication.dtos.AuthRequest;
import com.api.artezans.authentication.dtos.AuthResponse;
import com.api.artezans.config.security.JwtService;
import com.api.artezans.config.utils.SoftHasher;
import com.api.artezans.exceptions.ArtezanException;
import com.api.artezans.tokens.model.ArtezanToken;
import com.api.artezans.tokens.model.ArtezanVerificationToken;
import com.api.artezans.tokens.service.interfaces.ArtezanTokenService;
import com.api.artezans.tokens.service.interfaces.ArtezanVerificationTokenService;
import com.api.artezans.users.models.User;
import com.api.artezans.users.models.enums.AccountState;
import com.api.artezans.users.services.UserService;
import com.api.artezans.utils.ApiResponse;
import com.api.artezans.utils.ArtezanUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.api.artezans.utils.ArtezanUtils.BEARER;
import static com.api.artezans.utils.ArtezanUtils.DEACTIVATED;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${frontend_url}")
    private String frontendUrl;

    private final AuthenticationManager authenticationManager;
    private final ArtezanTokenService artezanTokenService;
    private final ArtezanVerificationTokenService artezanVerificationTokenService;
    private final LogoutService logoutService;
    private final ObjectMapper objectMapper;
    private final SoftHasher softHash;
    private final PasswordEncoder passwordEncoder;

    private final UserService userService;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    @Transactional(propagation = REQUIRED)
    public AuthResponse authenticateAndGetToken(AuthRequest authRequest) {
        final User user = userService.findUserByEmail(authRequest.emailAddress());

        if (user.getAccountState().equals(AccountState.NOT_VERIFIED)) {
            unverifiedUserEmailAddress(user);
        } else if (user.getAccountState().equals(AccountState.DEACTIVATED)) {
            return AuthResponse.builder()
                    .message(DEACTIVATED)
                    .build();
        } else if (user.getAccountState().equals(AccountState.VERIFIED)) {
            return login(authRequest, user);
        }
        throw new ArtezanException("Unusual error logging in");
    }

    private AuthResponse login(AuthRequest authRequest, User user) {
        JwtService.Tokens tokens;
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.emailAddress(),
                            authRequest.password()));
            if (authentication.isAuthenticated()) {
                tokens = jwtService.generateToken(authentication);
                saveTokenToDatabase(user, tokens);
            } else
                throw new ArtezanException("Authentication failed");
            return AuthResponse.builder()
//                    .user(new UserDTO(user))
                    .user(userMapper.toDTO(user))
                    .message("Authentication successful")
                    .accessToken(tokens.accessToken())
                    .refreshToken(tokens.refreshToken())
                    .build();
        } catch (Exception ex) {
            throw new ArtezanException(ex.getMessage());
        }
    }

    private void saveTokenToDatabase(User user, JwtService.Tokens tokens) {
        ArtezanToken token = ArtezanToken.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .revoked(false)
                .user(user)
                .build();
        artezanTokenService.saveToken(token);
    }

    private void unverifiedUserEmailAddress(User user) {
        ArtezanVerificationToken verificationToken = artezanVerificationTokenService
                .findByEmail(user.getEmailAddress());
        if (verificationToken.isExpired()) {
            updateTokenAndResendVerificationMail(user);
            throw new ArtezanException("New verification email has been sent to your email address");
        } else {
            Duration duration = Duration.between(LocalDateTime.now(), verificationToken.getExpireAt());
            throw new ArtezanException("Your verification token is still valid for the next " +
                    duration.toMinutes() + " minute(s). Check your registered email to get your email verified");
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
        String url = ArtezanUtils.getUrl(
                softHash.encode(emailAddress), // new BCryptPasswordEncoder().encode(emailAddress);
                generatedToken,
                preUrl);

        userService.sendMail(user, url);
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

        final String email = jwtService.extractUsername(refreshToken);
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





//    @Override
//    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        logoutService.logout(request, response);
//    }
//
//    @Override
//    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        final String authHeader = request.getHeader(AUTHORIZATION);
//        if (!StringUtils.hasText(authHeader) ||
//                !StringUtils.startsWithIgnoreCase(authHeader, BEARER))
//            return;
//        final String refreshToken = authHeader.substring(7);
//        if (jwtService.validateToken(refreshToken)) {
//            final String email = jwtService.extractUsername(refreshToken);
//            if (StringUtils.hasText(email)) {
//                final User user = userService.findUserByEmail(email);
//                final String accessToken = jwtService.accessToken(user);
//                final ArtezanToken token = artezanTokenService.getValidTokenByAnyToken(refreshToken)
//                        .orElseThrow(() -> new ArtezanException("Token could not be found"));
//                token.setAccessToken(accessToken);
//                artezanTokenService.saveToken(token);
//                final JwtService.Tokens newTokens = new JwtService.Tokens(accessToken, refreshToken);
//                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                objectMapper.writeValue(response.getOutputStream(), newTokens);
//                // new ObjectMapper().writeValue(response.getOutputStream(), newTokens);
//            }
//        }
//    }
}





