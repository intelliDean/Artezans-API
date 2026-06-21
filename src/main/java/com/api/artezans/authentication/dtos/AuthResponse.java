package com.api.artezans.authentication.dtos;

import com.api.artezans.users.dto.UserDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String message,
        String accessToken,
        String refreshToken,
        UserDTO user
) {
}