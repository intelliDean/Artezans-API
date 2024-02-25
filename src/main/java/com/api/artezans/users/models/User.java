package com.api.artezans.users.models;

import com.api.artezans.notifications.app_notification.model.AppNotification;
import com.api.artezans.users.models.enums.AccountState;
import com.api.artezans.users.models.enums.Role;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.api.artezans.utils.TaskHubUtils.NUMBER_MESSAGE;
import static com.api.artezans.utils.TaskHubUtils.VALID_NUMBER;
import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Setter
@Getter
@Builder
@Entity
@Validated
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String stripeId;

    @Column(nullable = false, updatable = false)
    private String firstName;

    @Column(nullable = false, updatable = false)
    private String lastName;

    @Column(unique = true, nullable = false, updatable = false)
    private String emailAddress;

    @Column(nullable = false)
    private String password;

    @Pattern(regexp = VALID_NUMBER, message = NUMBER_MESSAGE)
    private String phoneNumber;

    @OneToOne(targetEntity = Address.class, cascade = ALL, fetch = EAGER)
    @JoinColumn(name = "address_id")
    private Address address;

    private String profileImage;

    private boolean isEnabled;

    @Enumerated(EnumType.STRING)
    private AccountState accountState;

    private LocalDate deactivatedAt;

    @CreatedDate
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private final LocalDateTime registeredAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    @OneToMany(mappedBy = "recipient", fetch = LAZY)
    List<AppNotification> appNotificationList = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id) && Objects.equals(emailAddress, user.emailAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, emailAddress);
    }
}
