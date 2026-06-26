package com.api.artezans.tokens.model;

import com.api.artezans.users.models.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.GenerationType.IDENTITY;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "artezan_token",
        indexes = {
                @Index(name = "idx_artezan_token_access",  columnList = "accessToken"),
                @Index(name = "idx_artezan_token_refresh", columnList = "refreshToken"),
                @Index(name = "idx_artezan_token_user",    columnList = "user_id")
        }
)
public class ArtezanToken {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512)
    private String accessToken;

    @Column(length = 512)
    private String refreshToken;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean revoked;

    @Column
    private LocalDateTime revokedAt;

//    @Column(nullable = false)     //expiration is not needed here as the tokens carry their expiration
//    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}