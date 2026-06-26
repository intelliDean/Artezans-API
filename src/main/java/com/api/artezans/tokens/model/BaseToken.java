package com.api.artezans.tokens.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseToken {

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(nullable = false)
    private String emailAddress;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean revoked;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean expired;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEEE, d MMMM, yyyy hh:mm:ssa")
    private LocalDateTime generatedAt;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEEE, d MMMM, yyyy hh:mm:ssa")
    private LocalDateTime expireAt;

    public boolean isExpired() {
        return expireAt != null && expireAt.isBefore(LocalDateTime.now());
    }

    @PrePersist
    @PreUpdate
    public void updateExpiredState() {
        this.expired = isExpired();
    }
}

