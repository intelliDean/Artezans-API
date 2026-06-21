package com.api.artezans.authentication.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import static com.api.artezans.utils.ArtezanUtils.*;

@Builder
public record AuthRequest(
        @Email(message = EMAIL_ERROR_MSG)
        @NotNull(message = "Email Address" + NOT_NULL)
        @NotBlank(message = "Email Address" + NOT_BLANK)
        String emailAddress,

        @NotNull(message = "Password" + NOT_NULL)
        @NotBlank(message = "Password" + NOT_BLANK)
        @Pattern(regexp = VALID_PASSWORD, message = PASSWORD_MESSAGE)
        String password
) {}