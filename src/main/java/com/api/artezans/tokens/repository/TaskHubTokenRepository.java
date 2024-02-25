package com.api.artezans.tokens.repository;

import com.api.artezans.tokens.model.TaskHubToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskHubTokenRepository extends JpaRepository<TaskHubToken, Long> {
    @Query("""
            select token from TaskHubToken token
            where token.accessToken = :anyToken or token.refreshToken = :anyToken and token.revoked = false
            """)
    Optional<TaskHubToken> findValidTokenByToken(String anyToken);

    @Query("""
            select tokens from TaskHubToken tokens
            where tokens.revoked = true
            """)
    List<TaskHubToken> findAllInvalidTokens();

    @Query(value = """
            select token from TaskHubToken token
            where token.user.id = :userId and token.revoked = false
            """)
    List<TaskHubToken> findAllTokenByUserId(Long userId);
}
