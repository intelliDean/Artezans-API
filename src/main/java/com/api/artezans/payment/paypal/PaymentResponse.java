package com.api.artezans.payment.paypal;

public record PaymentResponse(

        String paymentId,

        String payerId
) {}
