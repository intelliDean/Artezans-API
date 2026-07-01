package com.api.artezans.review.data.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
    @NotNull(message = "Booking ID is required")
    Long bookingId,

    @NotBlank(message = "Provider email is required")
    String providerEmail,

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    int rating,

    @NotBlank(message = "Comment is required")
    String comment
) {}
