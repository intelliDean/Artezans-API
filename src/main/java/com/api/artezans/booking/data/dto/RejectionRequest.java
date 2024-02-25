package com.api.artezans.booking.data.dto;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectionRequest {

    private Long bookingId;

    private String rejectionReason;
}
