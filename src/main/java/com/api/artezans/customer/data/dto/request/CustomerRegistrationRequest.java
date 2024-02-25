package com.api.artezans.customer.data.dto.request;

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
public class CustomerRegistrationRequest {

    @NotNull(message = "First name" + NOT_NULL)
    @NotBlank(message = "First name" + NOT_BLANK)
    private String firstName;

    @NotNull(message = "Last name" + NOT_NULL)
    @NotBlank(message = "Last name" + NOT_BLANK)
    private String lastName;

    @NotNull(message = "Phone number" + NOT_NULL)
    @NotBlank(message = "Phone number" + NOT_BLANK)
    @Pattern(regexp = VALID_NUMBER, message = NUMBER_MESSAGE)
    private String phoneNumber;

    @Email(message = "Please provide a valid email address")
    @NotNull(message = "Email Address" + NOT_NULL)
    @NotBlank(message = "Email Address" + NOT_BLANK)
    private String emailAddress;

    @NotNull(message = "Password" + NOT_NULL)
    @NotBlank(message = "Password" + NOT_BLANK)
    @Pattern(regexp = VALID_PASSWORD, message = PASSWORD_MESSAGE)
    private String password;
}