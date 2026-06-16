package com.api.artezans.authentication.dtos;

import com.api.artezans.users.dto.UserDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

//@Setter
//@Getter
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_EMPTY)
//public class AuthResponse {
//
//    private UserDTO user;
//
//    private String accessToken;
//
//    private String refreshToken;
//
//    private String message;
//}

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        UserDTO user,
        String accessToken,
        String refreshToken,
        String message
) {}