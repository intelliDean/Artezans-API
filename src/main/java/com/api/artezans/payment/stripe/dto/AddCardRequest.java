package com.api.artezans.payment.stripe.dto;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddCardRequest {
    private String customerId;
}
