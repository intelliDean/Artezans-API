package com.api.artezans.tokens.repository;

import com.api.artezans.tokens.model.ArtezanToken;
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
public interface ArtezanTokenRepository extends JpaRepository<ArtezanToken, Long> {
    @Query("""
            select token from ArtezanToken token
            where (token.accessToken = :anyToken or token.refreshToken = :anyToken)
              and token.revoked = false
            """) //and token.expiresAt > CURRENT_TIMESTAMP
    Optional<ArtezanToken> findValidTokenByToken(String anyToken);

    @Query(value = """
            select token from ArtezanToken token
            where token.user.id = :userId and token.revoked = false
            """)
    List<ArtezanToken> findAllTokenByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE ArtezanToken t SET t.revoked = true, t.revokedAt = :revokedAt
        WHERE t.user.id = :userId
        AND t.revoked = false
        """)
    void revokeAllTokensByUserId(@Param("userId") Long userId, @Param("revokedAt") LocalDateTime revokedAt);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM ArtezanToken t
        WHERE t.revoked = true
        """) //OR t.expiresAt < :now
    void deleteAllInvalidTokens(); //@Param("now") LocalDateTime now
}
