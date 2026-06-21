package com.api.artezans.tokens.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Setter
@Getter
@Builder
@Entity
@Table(name = "artezan_verification_token")
@NoArgsConstructor
@AllArgsConstructor
public class ArtezanVerificationToken {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String token;

    private String emailAddress;

    private boolean revoked;

    @CreatedDate
    private LocalDateTime generatedAt;

    private LocalDateTime expireAt;

    private boolean expired;

    public boolean isExpired() {
        this.expired = expireAt.isBefore(LocalDateTime.now());
        return expired;
    }
}
