package com.api.artezans.password.data.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import static com.api.artezans.utils.TaskHubUtils.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotNull(message = "Old Password" + NOT_NULL)
    @NotBlank(message = "Old Password" + NOT_BLANK)
    @Pattern(regexp = VALID_PASSWORD, message = PASSWORD_MESSAGE)
    private String oldPassword;

    @NotNull(message = "New Password" + NOT_NULL)
    @NotBlank(message = "New Password" + NOT_BLANK)
    @Pattern(regexp = VALID_PASSWORD, message = PASSWORD_MESSAGE)
    private String newPassword;
}
