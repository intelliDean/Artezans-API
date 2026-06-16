package com.api.artezans.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ArtezanException extends RuntimeException {

    private final HttpStatus status;

    public ArtezanException() {
        this("An error occurred");
    }

    public ArtezanException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public ArtezanException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
