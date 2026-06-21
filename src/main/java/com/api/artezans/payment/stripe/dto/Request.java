package com.api.artezans.payment.stripe.dto;


import jakarta.validation.constraints.*;

public record Request(

        @NotNull
        @Min(4)
        Long amount,

        @Email
        String email,

        @NotBlank
        @Size(min = 5, max = 200)
        String productName) {
}