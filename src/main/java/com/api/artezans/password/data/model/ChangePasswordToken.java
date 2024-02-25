package com.api.artezans.password.data.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ChangePasswordToken {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String newPassword;

    private String oldPassword;

    private String emailAddress;

    private String token;

    private final LocalDateTime generatedAt = LocalDateTime.now();

    private boolean expired;

    private boolean revoked;

    public void checkTokenExpiration() {
        expired = generatedAt.plusHours(24).isBefore(LocalDateTime.now());
    }
}
