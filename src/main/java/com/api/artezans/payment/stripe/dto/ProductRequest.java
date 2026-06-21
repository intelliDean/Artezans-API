package com.api.artezans.payment.stripe.dto;

import lombok.Builder;

@Builder
public record ProductRequest(

        String serviceName,

        String serviceDescription,

        boolean isActive,

        String serviceProviderStripeId,

        Long servicePricePerUnit) {
}