package com.api.artezans.customer.data.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import static com.api.artezans.utils.ArtezanUtils.*;


public record CustomerUpdateRequest(

        @NotNull(message = "Street Number" + NOT_NULL)
        @NotBlank(message = "Street Number" + NOT_BLANK)
        String streetNumber,

        @NotNull(message = "Street Name" + NOT_NULL)
        @NotBlank(message = "Street Name" + NOT_BLANK)
        String streetName,

        @NotNull(message = "Suburb" + NOT_NULL)
        @NotBlank(message = "Suburb" + NOT_BLANK)
        String suburb,

        @NotNull(message = "State" + NOT_NULL)
        @NotBlank(message = "State" + NOT_BLANK)
        String state,

        @NotNull(message = "Post Code" + NOT_NULL)
        @NotBlank(message = "Post Code" + NOT_BLANK)
        String postCode,

        String unitNumber
) {
}