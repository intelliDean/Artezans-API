package com.api.artezans.payment.stripe.dto;

import com.stripe.model.Address;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record StripeResponse (

        String id,

        String name,

        String email,

        String phone,

        String description,

        BigDecimal balance,

        LocalDateTime created,

        Address address
) {}
