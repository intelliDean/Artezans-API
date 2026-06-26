package com.api.artezans.tokens.service;

import com.api.artezans.tokens.model.ArtezanToken;
import com.api.artezans.tokens.repository.ArtezanTokenRepository;
import com.api.artezans.tokens.service.interfaces.ArtezanTokenService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArtezanTokenServiceImpl implements ArtezanTokenService {

    private final ArtezanTokenRepository artezanTokenRepository;

    @Override
    public void saveToken(ArtezanToken token) {
        artezanTokenRepository.save(token);
    }

    @Override
    public Optional<ArtezanToken> getValidTokenByAnyToken(String anyToken) {
        return artezanTokenRepository.findValidTokenByToken(anyToken);
    }

    @Override
    public void revokeToken(String accessToken) {
        getValidTokenByAnyToken(accessToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    token.setRevokedAt(LocalDateTime.now());
                    artezanTokenRepository.save(token);
                });
    }


    @Override
    public void revokeAllUserTokens(Long userId) {
        artezanTokenRepository.revokeAllTokensByUserId(userId, LocalDateTime.now());
    }

    @Override
    public boolean isTokenValid(String anyToken) {
        return getValidTokenByAnyToken(anyToken)
                .map(token -> !token.isRevoked()) //&& token.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?", zone = "Australia/Sydney")
    public void deleteAllRevokedTokens() {
        artezanTokenRepository.deleteAllInvalidTokens();
    }
}