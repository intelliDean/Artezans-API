package com.api.artezans.password.forgot_password.services;

import com.api.artezans.exceptions.TokenException;
import com.api.artezans.password.forgot_password.model.ForgotPasswordToken;
import com.api.artezans.password.forgot_password.repository.ForgotPasswordTokenRepository;
import com.api.artezans.users.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ForgotPasswordTokenServiceImpl implements ForgotPasswordTokenService {

    private final ForgotPasswordTokenRepository forgotPasswordTokenRepository;

    @Override
    public void saveToken(User user, String token) {
        forgotPasswordTokenRepository.save(
                ForgotPasswordToken.builder()
                        .token(token)
                        .user(user)
                        .emailAddress(user.getEmailAddress())
                        .expireAt(LocalDateTime.now().plusHours(24))
                        .build()
        );
    }

    @Override
    public void validatePasswordResetToken(String token) {
        ForgotPasswordToken passwordToken = forgotPasswordTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new TokenException("Invalid password reset token"));

        if (passwordToken.isExpired()) {
            throw new TokenException("Password reset token has expired");
        }
        if (passwordToken.isRevoked()) {
            throw new TokenException("Password reset token has been revoked");
        }
    }

    @Override
    public Optional<User> findUserByPasswordToken(String token) {
        return forgotPasswordTokenRepository.findByToken(token)
                .map(ForgotPasswordToken::getUser);
    }

    @Override
    public void revokeToken(ForgotPasswordToken token) {
        token.setRevoked(true);
        forgotPasswordTokenRepository.save(token);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?", zone = "Australia/Sydney")
    public void deleteAllInvalidTokens() {
        forgotPasswordTokenRepository.deleteAllInvalidTokens(LocalDateTime.now());
    }

    @Override
    public boolean existsValidTokenGeneratedSince(String email, LocalDateTime now, LocalDateTime cooldownLimit) {
        return forgotPasswordTokenRepository.existsValidTokenGeneratedSince(email, now, cooldownLimit);
    }
}