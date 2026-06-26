package com.api.artezans.tokens.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static jakarta.persistence.GenerationType.IDENTITY;


@Setter
@Getter
@SuperBuilder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "artezan_verification_token",
        indexes = {
                @Index(name = "idx_verification_token", columnList = "token"),
                @Index(name = "idx_verification_email", columnList = "emailAddress")
        }
)
public class ArtezanVerificationToken extends BaseToken {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;


}