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
@Table(name = "service_providers")
public class ServiceProvider {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_identity_id")  // nullable — identity docs may come later
    private UserIdentity userIdentity;
}