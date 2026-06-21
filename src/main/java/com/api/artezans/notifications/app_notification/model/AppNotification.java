package com.api.artezans.notifications.app_notification.model;

import com.api.artezans.users.models.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.GenerationType.IDENTITY;

//@Entity
//@Data
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//public class AppNotification {
//    @Id
//    @GeneratedValue(strategy = IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, columnDefinition = "TEXT")
//    private String message;
//
//    @JsonIgnore
//    @ManyToOne(cascade = PERSIST)
//    @JoinColumn(name = "recipient_id", nullable = false)
//    private User recipient;
//
//    private LocalDateTime notificationTime;
//}


@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "app_notifications", indexes = {
        @Index(name = "idx_notification_recipient", columnList = "recipient_id"),
        @Index(name = "idx_notification_read", columnList = "read")
})
public class AppNotification {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean read = false;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime notificationTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppNotification n)) return false;
        return Objects.equals(id, n.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}