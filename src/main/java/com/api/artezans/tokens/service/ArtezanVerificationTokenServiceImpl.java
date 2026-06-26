package com.api.artezans.tokens.service;

import com.api.artezans.exceptions.ArtezanException;
import com.api.artezans.tokens.model.ArtezanVerificationToken;
import com.api.artezans.tokens.repository.ArtezanVerificationTokenRepository;
import com.api.artezans.tokens.service.interfaces.ArtezanVerificationTokenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class ArtezanVerificationTokenServiceImpl implements ArtezanVerificationTokenService {
    private final ArtezanVerificationTokenRepository artezanVerificationTokenRepository;


    @Override
    public void saveToken(ArtezanVerificationToken verificationToken) {
        ArtezanVerificationToken token = artezanVerificationTokenRepository
                .saveAndFlush(verificationToken);
        log.info("Verification token saved for email: {}", token.getEmailAddress());
    }

    @Override
    public ArtezanVerificationToken findByTokenAndEmail(String token, String email) {
        return artezanVerificationTokenRepository
                .findValidVerificationTokenByTokenAndEmail(token, email, LocalDateTime.now())
                .orElseThrow(() -> new ArtezanException("Token does not exist"));
    }


    @Override
    public void deleteToken(ArtezanVerificationToken verificationToken) {
        artezanVerificationTokenRepository.delete(verificationToken);
    }

    @Override
    public boolean isValid(ArtezanVerificationToken verificationToken) {
        return verificationToken != null
                && !verificationToken.isExpired()
                && !verificationToken.isRevoked();
    }


    @Override
    public ArtezanVerificationToken findByToken(String token) {
        return artezanVerificationTokenRepository
                .findValidVerificationTokenByToken(token, LocalDateTime.now())
                .orElseThrow(() -> new ArtezanException("Token could not be found"));
    }

    @Override
    public ArtezanVerificationToken findByEmail(String email) {
        return artezanVerificationTokenRepository.findByEmailAddress(email)
                .orElseThrow(() -> new ArtezanException("Token not found"));
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?", zone = "Australia/Sydney")
    public void deleteAllInvalidTokens() {
        artezanVerificationTokenRepository.deleteAllInvalidTokens(LocalDateTime.now());
    }

    @Override
    public boolean existsValidTokenGeneratedSince(String email, LocalDateTime now, LocalDateTime cooldownLimit) {
        return artezanVerificationTokenRepository.existsValidTokenGeneratedSince(email, now, cooldownLimit);
    }
}
