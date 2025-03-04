package com.api.artezans.tokens.service.interfaces;


import com.api.artezans.tokens.model.TaskHubToken;

import java.util.Optional;

public interface TaskHubTokenService {
    void saveToken(TaskHubToken taskHubToken);

    Optional<TaskHubToken> getValidTokenByAnyToken(String anyToken);

    void revokeToken(String accessToken);
    void revokeToken(Long userId);

    boolean isTokenValid(String anyToken);
}
