package com.api.artezans.payment.stripe.dto;


import lombok.Builder;

@Builder
public record Response (

        String intentID,

        String clientSecret
) {}