package com.api.artezans.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TaskHubException extends RuntimeException{

    private final HttpStatus status;

    public TaskHubException() {
        this("An error occurred");
    }

    public TaskHubException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public TaskHubException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
