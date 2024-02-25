package com.api.artezans.tokens.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import task.hub.exceptions.TaskHubException;
import task.hub.user.tokens.model.TaskHubVerificationToken;
import task.hub.user.tokens.repository.TaskHubVerificationTokenRepository;
import task.hub.user.tokens.service.interfaces.TaskHubVerificationTokenService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class TaskHubVerificationTokenServiceImpl implements TaskHubVerificationTokenService {
    private final TaskHubVerificationTokenRepository taskHubVerificationTokenRepository;

    @Override
    public void saveToken(TaskHubVerificationToken verificationToken) {
        TaskHubVerificationToken token = taskHubVerificationTokenRepository.saveAndFlush(verificationToken);
        log.info("saved token: {}", token.getToken());
        log.info("saved token email: {}", token.getEmailAddress());
    }

    @Override
    public void deleteToken(TaskHubVerificationToken verificationToken) {
        taskHubVerificationTokenRepository.delete(verificationToken);
    }

    @Override
    public boolean isValid(TaskHubVerificationToken verificationToken) {
        return verificationToken != null && !verificationToken.isExpired();
    }

    @Override
    public TaskHubVerificationToken findByTokenAndEmail(String token, String email) {
        return taskHubVerificationTokenRepository.findValidVerificationTokenByTokenAndEmail(token, email)
                .orElseThrow(() -> new TaskHubException("Token could not be found"));
    }

    @Override
    public TaskHubVerificationToken findByToken(String token) {
        return taskHubVerificationTokenRepository.findValidVerificationTokenByToken(token)
                .orElseThrow(() -> new TaskHubException("Token could not be found"));
    }

    @Override
    public TaskHubVerificationToken findByEmail(String email) {
        return taskHubVerificationTokenRepository.findByEmailAddress(email)
                .orElse(null);
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "Australia/Sydney") //scheduled to run every midnight
    private void deleteAllInvalidTokens() {
        final List<TaskHubVerificationToken> allRevokedTokens =
                taskHubVerificationTokenRepository.findAllInvalidTokens();
        if (!allRevokedTokens.isEmpty()) {
            taskHubVerificationTokenRepository.deleteAll(allRevokedTokens);
        }
    }

    @Scheduled(cron = "0 0 * * * ?", zone = "Australia/Sydney")
    private void setExpiredToken() {
        final List<TaskHubVerificationToken> tokens = taskHubVerificationTokenRepository.findAllValidTokens();
        tokens.stream().filter(token -> token.getExpireAt().isBefore(LocalDateTime.now())
        ).forEach(init -> init.setExpired(true));
        taskHubVerificationTokenRepository.saveAll(tokens);
    }
}

