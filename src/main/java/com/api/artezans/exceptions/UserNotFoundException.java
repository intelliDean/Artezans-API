package com.api.artezans.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends TaskHubException {

    public UserNotFoundException() {
        this("User could not be found");
    }

    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
