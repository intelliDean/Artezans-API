package com.api.artezans.booking.data.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceResponse {

    private String serviceProvider;

    private String businessName;

    private String serviceCategory;

    private String serviceName;

    private String customerName;

    private final String serviceCharges = "15%";

    private BigDecimal subTotal;

    private BigDecimal total;

    private LocalDateTime bookedAt;

    private BigDecimal pricePerUnit;

    private int numberOfHoursWorked;

    private int numberOfDaysWorked;

    private final LocalDateTime generatedAt = LocalDateTime.now();
}