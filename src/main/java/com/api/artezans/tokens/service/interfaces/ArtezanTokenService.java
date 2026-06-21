package com.api.artezans.tokens.service.interfaces;

import com.api.artezans.tokens.model.ArtezanToken;
import java.util.Optional;

public interface ArtezanTokenService {
    void saveToken(ArtezanToken token);

    Optional<ArtezanToken> getValidTokenByAnyToken(String anyToken);

    void revokeToken(String accessToken);
    void revokeToken(Long userId);

    boolean isTokenValid(String anyToken);
}
