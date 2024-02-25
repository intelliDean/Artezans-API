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
public class TaskHubExceptionHandler {

    @ExceptionHandler(TaskHubException.class)
    public ResponseEntity<TaskHubExceptionResponse> handleException(
            TaskHubException exception
    ) {
        return ResponseEntity.badRequest()
                .body(
                        TaskHubExceptionResponse.builder()
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
        return ResponseEntity.badRequest().body(TaskHubExceptionResponse.builder()
                .message(e.getFieldError().getDefaultMessage().toUpperCase())
                .status(HttpStatus.BAD_REQUEST)
                .data(fieldErrors)
                .build());
    }

    @ExceptionHandler(UserNotAuthorizedException.class)
    public ResponseEntity<TaskHubExceptionResponse> handleException(
            UserNotAuthorizedException exception
    ) {
        return ResponseEntity.badRequest()
                .body(
                        TaskHubExceptionResponse.builder()
                                .message(exception.getMessage())
                                .status(exception.getStatus())
                                .build()
                );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<TaskHubExceptionResponse> handleException(
            UserNotFoundException exception
    ) {
        return ResponseEntity.badRequest()
                .body(
                        TaskHubExceptionResponse.builder()
                                .message(exception.getMessage())
                                .status(exception.getStatus())
                                .build()
                );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<TaskHubExceptionResponse> handleException(
            BadCredentialsException exception
    ) {
        return ResponseEntity.badRequest()
                .body(
                        TaskHubExceptionResponse.builder()
                                .message(exception.getMessage())
                                .status(HttpStatus.BAD_REQUEST)
                                .build()
                );
    }
}
