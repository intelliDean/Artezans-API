package com.api.artezans.payment.stripe.dto;

import com.stripe.model.Address;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StripeResponse {

    private String id;
    private String name;
    private String email;
    private String phone;
    private String description;
    private BigDecimal balance;
    private LocalDateTime created;
    private Address address;
}
