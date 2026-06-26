package com.api.artezans.tokens.service.interfaces;

import com.api.artezans.tokens.model.ArtezanToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ArtezanTokenService {
    void saveToken(ArtezanToken token);

    Optional<ArtezanToken> getValidTokenByAnyToken(String anyToken);

    void revokeToken(String accessToken);


    boolean isTokenValid(String anyToken);

    void revokeAllUserTokens(Long userId);

    void deleteAllRevokedTokens();

}