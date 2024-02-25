package com.api.artezans.tokens.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import task.hub.user.tokens.model.TaskHubVerificationToken;

import java.util.List;
import java.util.Optional;
@Repository
public interface TaskHubVerificationTokenRepository extends JpaRepository<TaskHubVerificationToken, Long> {
    @Query("""
            select verificationToken from TaskHubVerificationToken verificationToken
            where verificationToken.token = :token and verificationToken.emailAddress = :email
            and verificationToken.revoked = false and verificationToken.expired = false
            """)
    Optional<TaskHubVerificationToken> findValidVerificationTokenByTokenAndEmail(String token, String email);

    @Query("""
            select verificationToken from TaskHubVerificationToken verificationToken
            where verificationToken.token = :token and verificationToken.revoked = false
            and verificationToken.expired = false
            """)
    Optional<TaskHubVerificationToken> findValidVerificationTokenByToken(String token);

    @Query("""
            select tokens from TaskHubVerificationToken tokens
            where tokens.revoked = true or tokens.expired = true
            """)
    List<TaskHubVerificationToken> findAllInvalidTokens();

    @Query("""
            select token from TaskHubVerificationToken token
            where token.revoked = false and token.expired = false
            """)
    List<TaskHubVerificationToken> findAllValidTokens();


    Optional<TaskHubVerificationToken> findByEmailAddress(String emailAddress);
}
