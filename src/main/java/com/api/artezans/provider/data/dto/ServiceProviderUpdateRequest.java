package com.api.artezans.provider.data.dto;

import com.api.artezans.provider.data.model.enums.IdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import static com.api.artezans.utils.ArtezanUtils.*;

public record ServiceProviderUpdateRequest(

        @NotNull(message = "Street Number" + NOT_NULL)
        @NotBlank(message = "Street Number" + NOT_BLANK)
        String streetNumber,

        @NotNull(message = "Street Name" + NOT_NULL)
        @NotBlank(message = "Street Name" + NOT_BLANK)
        String streetName,

        @NotNull(message = "Suburb" + NOT_NULL)
        @NotBlank(message = "Suburb" + NOT_BLANK)
        String city,

        @NotNull(message = "State" + NOT_NULL)
        @NotBlank(message = "State" + NOT_BLANK)
        String state,

        @NotNull(message = "Post Code" + NOT_NULL)
        @NotBlank(message = "Post Code" + NOT_BLANK)
        String postCode,

        String unitNumber,

        MultipartFile idImage,

        String idType,

        String idNumber) {
}
