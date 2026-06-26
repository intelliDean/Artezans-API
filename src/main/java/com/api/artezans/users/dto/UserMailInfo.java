package com.api.artezans.users.dto;

import com.api.artezans.users.models.User;
import lombok.Builder;

@Builder
public record UserMailInfo(String firstName, String emailAddress) {

    public UserMailInfo(User user) {
        this(user.getFirstName(), user.getEmailAddress());
    }
}