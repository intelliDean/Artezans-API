package com.api.artezans.password.change_password.repository;

import com.api.artezans.password.change_password.model.ChangePasswordToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChangePasswordTokenRepository extends JpaRepository<ChangePasswordToken, Long> {

    @Query(value = """
            select cpt from ChangePasswordToken cpt
            where cpt.revoked = true
            """)
    List<ChangePasswordToken> findAllRevokedTokens();

    Optional<ChangePasswordToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM ChangePasswordToken t
            WHERE t.revoked = true
            OR t.expireAt < :now
            """)
    void deleteAllInvalidTokens(@Param("now") LocalDateTime now);
}