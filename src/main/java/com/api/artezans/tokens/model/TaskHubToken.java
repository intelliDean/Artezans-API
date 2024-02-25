package com.api.artezans.tokens.model;

import jakarta.persistence.*;
import lombok.*;
import task.hub.user.users.models.User;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskHubToken {
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
