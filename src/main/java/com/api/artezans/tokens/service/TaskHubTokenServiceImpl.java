package com.api.artezans.tokens.service;

import com.api.artezans.tokens.model.TaskHubToken;
import com.api.artezans.tokens.repository.ArtezanTokenRepository;
import com.api.artezans.tokens.service.interfaces.TaskHubTokenService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@AllArgsConstructor
public class TaskHubTokenServiceImpl implements TaskHubTokenService {

    private final ArtezanTokenRepository artezanTokenRepository;


    @Override
    public void saveToken(TaskHubToken taskHubToken) {
        artezanTokenRepository.save(taskHubToken);
    }

    @Override
    public Optional<TaskHubToken> getValidTokenByAnyToken(String anyToken) {
        return artezanTokenRepository.findValidTokenByToken(anyToken);
    }

    @Override
    public void revokeToken(String accessToken) {
        final TaskHubToken taskHubToken = getValidTokenByAnyToken(accessToken)
                .orElse(null);
        if (taskHubToken != null) {
            taskHubToken.setRevoked(true);
            artezanTokenRepository.save(taskHubToken);
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
                .map(taskHubToken -> !taskHubToken.isRevoked())
                .orElse(false);
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "Australia/Sydney") //schedule to run every midnight
    void deleteAllRevokedTokens() {
        final List<TaskHubToken> allRevokedTokens =
                artezanTokenRepository.findAllInvalidTokens();
        if (!allRevokedTokens.isEmpty()) {
            artezanTokenRepository.deleteAll(allRevokedTokens);
        }
    }
}
