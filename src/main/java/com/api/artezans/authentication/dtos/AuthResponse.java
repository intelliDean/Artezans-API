package com.api.artezans.authentication.dtos;

import com.api.artezans.users.dto.UserDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private UserDTO user;

    private String accessToken;

    private String refreshToken;

    private String message;
}