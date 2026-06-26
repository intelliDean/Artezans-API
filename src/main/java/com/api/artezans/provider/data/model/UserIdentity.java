package com.api.artezans.provider.data.model;

import com.api.artezans.provider.data.model.enums.IdType;
import com.api.artezans.provider.data.model.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Setter
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "user_identity",
        indexes = {
                @Index(name = "idx_user_identity_id_number", columnList = "idNumber")
        }
)
public class UserIdentity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String idNumber;

    @Column(nullable = false)
    private String idImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdType idType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

