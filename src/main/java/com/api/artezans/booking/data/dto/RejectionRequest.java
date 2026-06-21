package com.api.artezans.booking.data.dto;

import lombok.*;

@Builder
public record RejectionRequest(

        Long bookingId,

        String rejectionReason
) {}
