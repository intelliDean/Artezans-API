package com.api.artezans.tokens.service.interfaces;

import com.api.artezans.tokens.model.ArtezanVerificationToken;

import java.util.Optional;

public interface ArtezanVerificationTokenService {

    void saveToken(ArtezanVerificationToken token);

    boolean isValid(ArtezanVerificationToken token);

    void deleteToken(ArtezanVerificationToken token);

    ArtezanVerificationToken findByTokenAndEmail(String token, String email);

    ArtezanVerificationToken findByToken(String token);

    ArtezanVerificationToken findByEmail(String email);

    void deleteAllInvalidTokens();

    boolean existsValidTokenGeneratedSince(String email, java.time.LocalDateTime now, java.time.LocalDateTime cooldownLimit);
}


