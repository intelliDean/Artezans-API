package com.api.artezans.provider.data.model;

import com.api.artezans.users.models.User;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.GenerationType.IDENTITY;

@Setter
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProvider {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(targetEntity = User.class, cascade = ALL)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(targetEntity = UserIdentity.class, cascade = PERSIST)
    @JoinColumn(name = "user_identity_id")
    private UserIdentity userIdentity;
}
