package com.api.artezans.password.forgot_password.repository;

import com.api.artezans.password.forgot_password.model.ForgotPasswordToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ForgotPasswordTokenRepository extends JpaRepository<ForgotPasswordToken, Long> {

    Optional<ForgotPasswordToken> findByToken(String token);

    @Query("""
            select count(t) > 0 from ForgotPasswordToken t
            where t.emailAddress = :email
              and t.revoked = false
              and t.expireAt > :now
              and t.generatedAt > :cooldownLimit
            """)
    boolean existsValidTokenGeneratedSince(
            @Param("email") String email,
            @Param("now") LocalDateTime now,
            @Param("cooldownLimit") LocalDateTime cooldownLimit);

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM ForgotPasswordToken t
            WHERE t.revoked = true
            OR t.expireAt < :now
            """)
    void deleteAllInvalidTokens(@Param("now") LocalDateTime now);
}