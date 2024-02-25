package com.api.artezans.notifications.app_notification.model;

import com.api.artezans.users.models.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppNotification {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @JsonIgnore
    @ManyToOne(cascade = PERSIST)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    private LocalDateTime notificationTime;
}
