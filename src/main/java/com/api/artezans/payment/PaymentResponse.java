package com.api.artezans.payment;

import lombok.*;

import java.util.Map;

@Setter
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class PaymentResponse {

    private String id;

    private Long amount;

    private String receiptEmail;

    private Map<String, Object> metadata;
}
