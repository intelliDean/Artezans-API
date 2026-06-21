package com.api.artezans.tokens.model;

import com.api.artezans.users.models.User;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "artezan_token")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtezanToken {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String accessToken;

    private String refreshToken;

    private boolean revoked;

    @ManyToOne(cascade = PERSIST)
    @JoinColumn(name = "user_id")
    private User user;
}
