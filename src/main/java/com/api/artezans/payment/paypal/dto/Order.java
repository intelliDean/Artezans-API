package com.api.artezans.payment.paypal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;


public record Order (

        @NotBlank
        @NotNull
        Double price,

        @NotBlank
        @NotNull
        String currency,

        String method,

        String intent,

        String description
) {}
