package com.api.artezans.tokens.repository;

import com.api.artezans.tokens.model.ArtezanVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtezanVerificationTokenRepository extends JpaRepository<ArtezanVerificationToken, Long> {
    @Query("""
            select verificationToken from ArtezanVerificationToken verificationToken
            where verificationToken.token = :token and verificationToken.emailAddress = :email
            and verificationToken.revoked = false and verificationToken.expired = false
            """)
    Optional<ArtezanVerificationToken> findValidVerificationTokenByTokenAndEmail(String token, String email);

    @Query("""
            select verificationToken from ArtezanVerificationToken verificationToken
            where verificationToken.token = :token and verificationToken.revoked = false
            and verificationToken.expired = false
            """)
    Optional<ArtezanVerificationToken> findValidVerificationTokenByToken(String token);

    @Query("""
            select tokens from ArtezanVerificationToken tokens
            where tokens.revoked = true or tokens.expired = true
            """)
    List<ArtezanVerificationToken> findAllInvalidTokens();

    @Query("""
            select token from ArtezanVerificationToken token
            where token.revoked = false and token.expired = false
            """)
    List<ArtezanVerificationToken> findAllValidTokens();

    Optional<ArtezanVerificationToken> findByEmailAddress(String emailAddress);
}
