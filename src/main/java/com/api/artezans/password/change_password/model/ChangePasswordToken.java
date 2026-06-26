package com.api.artezans.password.change_password.model;

import com.api.artezans.tokens.model.BaseToken;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "change_password_token",
        indexes = {
                @Index(name = "idx_change_password_token", columnList = "token"),
                @Index(name = "idx_change_password_email", columnList = "emailAddress")
        }
)
public class ChangePasswordToken extends BaseToken {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String newPasswordHash;
}