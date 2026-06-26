package com.api.artezans.password.forgot_password.model;

import com.api.artezans.tokens.model.BaseToken;
import com.api.artezans.users.models.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import static jakarta.persistence.GenerationType.IDENTITY;


@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "forgot_password_token",
        indexes = {
                @Index(name = "idx_forgot_password_token", columnList = "token"),
                @Index(name = "idx_forgot_password_user",  columnList = "user_id")
        }
)
public class ForgotPasswordToken extends BaseToken {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;
}