package com.api.artezans.payment;

import lombok.*;

import java.util.Map;


public record PaymentResponse(
        String id,
        Long amount,
        String receiptEmail,
        Map<String, Object> metadata) {
}
