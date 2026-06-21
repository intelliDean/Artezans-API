package com.api.artezans.payment.stripe.dto;

import lombok.Builder;

@Builder
public record PaymentIntentRequest(
        Long amount,
        String bookingId,
        String serviceName,
        String receiptEmail,
        String customerId,
        String productOwner,
        String productId) {
}