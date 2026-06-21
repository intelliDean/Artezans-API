package com.api.artezans.users.models;

import com.api.artezans.notifications.app_notification.model.AppNotification;
import com.api.artezans.users.models.enums.AccountState;
import com.api.artezans.users.models.enums.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.api.artezans.utils.ArtezanUtils.NUMBER_MESSAGE;
import static com.api.artezans.utils.ArtezanUtils.VALID_NUMBER;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Setter
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "emailAddress"),
                @Index(name = "idx_user_account_state", columnList = "accountState")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String stripeId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false, updatable = false)
    private String emailAddress;

    @Column(nullable = false)
    private String password;

    @Pattern(regexp = VALID_NUMBER, message = NUMBER_MESSAGE)
    @Column(unique = true)  // phone numbers should also be unique
    private String phoneNumber;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    private String profileImage;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean enabled = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountState accountState;

    private LocalDate deactivatedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEEE, d MMMM, yyyy hh:mm:ssa")
    private LocalDateTime registeredAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "recipient", fetch = LAZY)
    @Builder.Default
    private Set<AppNotification> appNotifications = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User user)) {
            return false;
        }
        return Objects.equals(id, user.id) && Objects.equals(emailAddress, user.emailAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, emailAddress);
    }
}