package com.api.artezans.password.data.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

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

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime generatedAt;

    private boolean expired;

    private boolean revoked;

    public void checkTokenExpiration() {
        expired = generatedAt.plusHours(24).isBefore(LocalDateTime.now());
    }
}
