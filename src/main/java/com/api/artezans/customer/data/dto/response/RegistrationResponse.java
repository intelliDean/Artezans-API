package com.api.artezans.customer.data.dto.response;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class RegistrationResponse {

    private Object data;
    private boolean isSuccessful;

}
