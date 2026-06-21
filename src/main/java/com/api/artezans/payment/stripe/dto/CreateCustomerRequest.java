package com.api.artezans.payment.stripe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateCustomerRequest(

        @NotBlank
        @NotNull
        String name,

        @NotBlank
        @NotNull
        String email,

        @NotBlank
        @NotNull String phone,

        @NotBlank
        @NotNull
        String description) {
}