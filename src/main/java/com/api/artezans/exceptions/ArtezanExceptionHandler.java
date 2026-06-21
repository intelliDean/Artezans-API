package com.api.artezans.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ArtezanExceptionHandler {

    @ExceptionHandler(ArtezanException.class)
    public ResponseEntity<ArtezanExceptionResponse> handleException(
            ArtezanException exception
    ) {
        return ResponseEntity.status(exception.getStatus())
                .body(
                        ArtezanExceptionResponse.builder()
                                .message(exception.getMessage())
                                .status(exception.getStatus())
                                .build()
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException thrown: {}", e.getLocalizedMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(ArtezanExceptionResponse.builder()
                .message(e.getFieldError().getDefaultMessage().toUpperCase())
                .status(HttpStatus.BAD_REQUEST)
                .data(fieldErrors)
                .build());
    }

    @ExceptionHandler(UserNotAuthorizedException.class)
    public ResponseEntity<ArtezanExceptionResponse> handleException(
            UserNotAuthorizedException exception
    ) {
        return ResponseEntity.status(exception.getStatus())
                .body(
                        ArtezanExceptionResponse.builder()
                                .message(exception.getMessage())
                                .status(exception.getStatus())
                                .build()
                );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ArtezanExceptionResponse> handleException(
            UserNotFoundException exception
    ) {
        return ResponseEntity.status(exception.getStatus())
                .body(
                        ArtezanExceptionResponse.builder()
                                .message(exception.getMessage())
                                .status(exception.getStatus())
                                .build()
                );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ArtezanExceptionResponse> handleException(
            BadCredentialsException exception
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        ArtezanExceptionResponse.builder()
                                .message(exception.getMessage())
                                .status(HttpStatus.UNAUTHORIZED)
                                .build()
                );
    }
}
