package com.api.artezans.payment.stripe.dto;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentIntentRequest {

    private Long amount;

    private String bookingId;

    private String serviceName;

    private String receiptEmail;

    private String customerId;

    private String productOwner;

    private String productId;
}