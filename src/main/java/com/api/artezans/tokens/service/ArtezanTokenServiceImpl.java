package com.api.artezans.tokens.service;

import com.api.artezans.tokens.model.ArtezanToken;
import com.api.artezans.tokens.repository.ArtezanTokenRepository;
import com.api.artezans.tokens.service.interfaces.ArtezanTokenService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
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
        final ArtezanToken token = getValidTokenByAnyToken(accessToken)
                .orElse(null);
        if (token != null) {
            token.setRevoked(true);
            artezanTokenRepository.save(token);
        }
    }

    @Override
    public void revokeToken(Long userId) {
        artezanTokenRepository.saveAll(
                artezanTokenRepository.findAllTokenByUserId(userId)
                .stream()
                .peek(token -> token.setRevoked(true))
                .toList()
        );
    }

    @Override
    public boolean isTokenValid(String anyToken) {
        return getValidTokenByAnyToken(anyToken)
                .map(token -> !token.isRevoked())
                .orElse(false);
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "Australia/Sydney") //schedule to run every midnight
    void deleteAllRevokedTokens() {
        final List<ArtezanToken> allRevokedTokens =
                artezanTokenRepository.findAllInvalidTokens();
        if (!allRevokedTokens.isEmpty()) {
            artezanTokenRepository.deleteAll(allRevokedTokens);
        }
    }
}
