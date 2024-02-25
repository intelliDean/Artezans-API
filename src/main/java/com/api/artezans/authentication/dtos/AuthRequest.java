package com.api.artezans.authentication.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import static com.api.artezans.utils.TaskHubUtils.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    @Email(message = EMAIL)
    @NotNull(message = "Email Address" + NOT_NULL)
    @NotBlank(message = "Email Address" + NOT_BLANK)
    private String emailAddress;

    @NotNull(message = "Password" + NOT_NULL)
    @NotBlank(message = "Password" + NOT_BLANK)
    @Pattern(regexp = VALID_PASSWORD, message = PASSWORD_MESSAGE)
    private String password;
}
