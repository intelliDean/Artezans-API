package com.api.artezans.payment.stripe.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    private String serviceName;

    private String serviceDescription;

    private boolean isActive;

    private String serviceProviderStripeId;

    private Long servicePricePerUnit;
}