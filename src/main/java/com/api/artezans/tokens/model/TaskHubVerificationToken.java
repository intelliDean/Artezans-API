package com.api.artezans.tokens.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Setter
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class TaskHubVerificationToken {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String token;

    private String emailAddress;

    private boolean revoked;

    @CreatedDate
    private  LocalDateTime generatedAt;// = LocalDateTime.now();

    private  LocalDateTime expireAt;// = LocalDateTime.now().plusHours(24);

    private boolean expired;


    public boolean isExpired() {
        this.expired = expireAt.isBefore(LocalDateTime.now());
        return expired;
    }
}
