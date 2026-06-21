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

@Slf4j
@Service
@AllArgsConstructor
public class ArtezanVerificationTokenServiceImpl implements ArtezanVerificationTokenService {
    private final ArtezanVerificationTokenRepository artezanVerificationTokenRepository;

    @Override
    public void saveToken(ArtezanVerificationToken verificationToken) {
        ArtezanVerificationToken token = artezanVerificationTokenRepository.saveAndFlush(verificationToken);
        log.info("saved token: {}", token.getToken());
        log.info("saved token email: {}", token.getEmailAddress());
    }

    @Override
    public void deleteToken(ArtezanVerificationToken verificationToken) {
        artezanVerificationTokenRepository.delete(verificationToken);
    }

    @Override
    public boolean isValid(ArtezanVerificationToken verificationToken) {
        return verificationToken != null && !verificationToken.isExpired();
    }

    @Override
    public ArtezanVerificationToken findByTokenAndEmail(String token, String email) {
        return artezanVerificationTokenRepository.findValidVerificationTokenByTokenAndEmail(token, email)
                .orElseThrow(() -> new ArtezanException("Token could not be found"));
    }

    @Override
    public ArtezanVerificationToken findByToken(String token) {
        return artezanVerificationTokenRepository.findValidVerificationTokenByToken(token)
                .orElseThrow(() -> new ArtezanException("Token could not be found"));
    }

    @Override
    public ArtezanVerificationToken findByEmail(String email) {
        return artezanVerificationTokenRepository.findByEmailAddress(email)
                .orElse(null);
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "Australia/Sydney") //scheduled to run every midnight
    void deleteAllInvalidTokens() {
        final List<ArtezanVerificationToken> allRevokedTokens =
                artezanVerificationTokenRepository.findAllInvalidTokens();
        if (!allRevokedTokens.isEmpty()) {
            artezanVerificationTokenRepository.deleteAll(allRevokedTokens);
        }
    }

    @Scheduled(cron = "0 0 * * * ?", zone = "Australia/Sydney")
    void setExpiredToken() {
        final List<ArtezanVerificationToken> tokens = artezanVerificationTokenRepository.findAllValidTokens();
        tokens.stream().filter(token -> token.getExpireAt().isBefore(LocalDateTime.now())
        ).forEach(init -> init.setExpired(true));
        artezanVerificationTokenRepository.saveAll(tokens);
    }
}
