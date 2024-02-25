package com.api.artezans.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotAuthorizedException extends TaskHubException {

    public UserNotAuthorizedException() {
        this("Unauthorized");
    }

    public UserNotAuthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
