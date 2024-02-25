package com.api.artezans.tokens.service;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import task.hub.user.tokens.model.TaskHubToken;
import task.hub.user.tokens.repository.TaskHubTokenRepository;
import task.hub.user.tokens.service.interfaces.TaskHubTokenService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TaskHubTokenServiceImpl implements TaskHubTokenService {

    private final TaskHubTokenRepository taskHubTokenRepository;


    @Override
    public void saveToken(TaskHubToken taskHubToken) {
        taskHubTokenRepository.save(taskHubToken);
    }

    @Override
    public Optional<TaskHubToken> getValidTokenByAnyToken(String anyToken) {
        return taskHubTokenRepository.findValidTokenByToken(anyToken);
    }

    @Override
    public void revokeToken(String accessToken) {
        final TaskHubToken taskHubToken = getValidTokenByAnyToken(accessToken)
                .orElse(null);
        if (taskHubToken != null) {
            taskHubToken.setRevoked(true);
            taskHubTokenRepository.save(taskHubToken);
        }
    }

    @Override
    public void revokeToken(Long userId) {
        taskHubTokenRepository.saveAll(
                taskHubTokenRepository.findAllTokenByUserId(userId)
                .stream()
                .peek(token -> token.setRevoked(true))
                .collect(Collectors.toList())
        );
    }


    @Override
    public boolean isTokenValid(String anyToken) {
        return getValidTokenByAnyToken(anyToken)
                .map(taskHubToken -> !taskHubToken.isRevoked())
                .orElse(false);
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "Australia/Sydney") //schedule to run every midnight
    private void deleteAllRevokedTokens() {
        final List<TaskHubToken> allRevokedTokens =
                taskHubTokenRepository.findAllInvalidTokens();
        if (!allRevokedTokens.isEmpty()) {
            taskHubTokenRepository.deleteAll(allRevokedTokens);
        }
    }
}
