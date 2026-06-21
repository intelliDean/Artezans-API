package com.api.artezans.tokens.repository;

import com.api.artezans.tokens.model.ArtezanToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtezanTokenRepository extends JpaRepository<ArtezanToken, Long> {
        @Query("""
        select token from ArtezanToken token
        where (token.accessToken = :anyToken or token.refreshToken = :anyToken) and token.revoked = false
        """)
        Optional<ArtezanToken> findValidTokenByToken(String anyToken);

        @Query("""
                        select tokens from ArtezanToken tokens
                        where tokens.revoked = true
                        """)
        List<ArtezanToken> findAllInvalidTokens();

        @Query(value = """
                        select token from ArtezanToken token
                        where token.user.id = :userId and token.revoked = false
                        """)
        List<ArtezanToken> findAllTokenByUserId(Long userId);
}
