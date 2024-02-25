package com.api.artezans.payment.paypal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @NotBlank
    @NotNull
    private Double price;

    @NotBlank
    @NotNull
    private String currency;

    private String method;

    private String intent;

    private String description;
}
