package com.api.artezans.customer.data.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import static com.api.artezans.utils.TaskHubUtils.*;


@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerUpdateRequest {

    @NotNull(message = "Street Number" + NOT_NULL)
    @NotBlank(message = "Street Number" + NOT_BLANK)
    private String streetNumber;

    @NotNull(message = "Street Name" + NOT_NULL)
    @NotBlank(message = "Street Name" + NOT_BLANK)
    private String streetName;

    @NotNull(message = "Suburb" + NOT_NULL)
    @NotBlank(message = "Suburb" + NOT_BLANK)
    private String suburb;

    @NotNull(message = "State" + NOT_NULL)
    @NotBlank(message = "State" + NOT_BLANK)
    private String state;

    @NotNull(message = "Post Code" + NOT_NULL)
    @NotBlank(message = "Post Code" + NOT_BLANK)
    private String postCode;

    private String unitNumber;
}