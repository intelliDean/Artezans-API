package com.api.artezans.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private User user;


    public Long getId() {
        return user.getId();
    }

    public String getStripeId() {
        return user.getStripeId();
    }

    public String getFirstName() {
        return user.getFirstName();
    }

    public String getLastName() {
        return user.getLastName();
    }

    public String getEmailAddress() {
        return user.getEmailAddress();
    }

    public String getPhoneNumber() {
        return user.getPhoneNumber();
    }

    public Address getAddress() {
        return user.getAddress();
    }

    public String getProfileImage() {
        return user.getProfileImage();
    }

    public boolean isEnabled() {
        return user.isEnabled();
    }

    public AccountState getAccountState() {
        return user.getAccountState();
    }

    public LocalDate getDeactivatedAt() {
        return user.getDeactivatedAt();
    }

    public LocalDateTime getRegisteredAt() {
        return user.getRegisteredAt();
    }

    public Set<Role> getRoles() {
        return user.getRoles();
    }

    public List<AppNotification> getAppNotificationList() {
        return user.getAppNotificationList();
    }
}
