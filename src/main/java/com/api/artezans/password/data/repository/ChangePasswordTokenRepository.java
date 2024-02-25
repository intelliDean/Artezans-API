package com.api.artezans.password.data.repository;

import com.api.artezans.password.data.model.ChangePasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChangePasswordTokenRepository extends JpaRepository<ChangePasswordToken, Long> {

    Optional<ChangePasswordToken> findByToken(String token);

    @Query(value = """
            select cpt from ChangePasswordToken cpt
            where cpt.revoked = true
            """)
    List<ChangePasswordToken> findAllRevokedTokens();
}
