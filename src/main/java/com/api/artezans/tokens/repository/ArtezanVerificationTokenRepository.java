package com.api.artezans.tokens.repository;

import com.api.artezans.tokens.model.ArtezanVerificationToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    //========


    @Query("""
            select t from ArtezanVerificationToken t
            where t.token = :token
            and t.emailAddress = :email
            and t.revoked = false
            and t.expireAt > :now
            """)
    Optional<ArtezanVerificationToken> findValidVerificationTokenByTokenAndEmail(
            @Param("token") String token,
            @Param("email") String email,
            @Param("now") LocalDateTime now);

    @Query("""
            select t from ArtezanVerificationToken t
            where t.token = :token
            and t.revoked = false
            and t.expireAt > :now
            """)
    Optional<ArtezanVerificationToken> findValidVerificationTokenByToken(
            @Param("token") String token,
            @Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM ArtezanVerificationToken t
            WHERE t.revoked = true
            OR t.expireAt < :now
            """)
    void deleteAllInvalidTokens(@Param("now") LocalDateTime now);

    Optional<ArtezanVerificationToken> findByEmailAddress(String emailAddress);

    @Query("""
            select count(t) > 0 from ArtezanVerificationToken t
            where t.emailAddress = :email
              and t.revoked = false
              and t.expireAt > :now
              and t.generatedAt > :cooldownLimit
            """)
    boolean existsValidTokenGeneratedSince(
            @Param("email") String email,
            @Param("now") LocalDateTime now,
            @Param("cooldownLimit") LocalDateTime cooldownLimit);
}
