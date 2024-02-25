package com.api.artezans.listings.data.dtos;

import com.api.artezans.listings.data.enums.AvailableDays;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Set;

import static com.api.artezans.utils.TaskHubUtils.*;


@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListingRequest {

    @NotNull(message = "Business Name" + NOT_NULL)
    @NotBlank(message = "Business Name" + NOT_BLANK)
    private String businessName;

    @NotNull(message = "Category" + NOT_NULL)
    @NotBlank(message = "Category" + NOT_BLANK)
    private String serviceCategory;

    @NotNull(message = "Service" + NOT_NULL)
    @NotBlank(message = "Service" + NOT_BLANK)
    private String serviceName;

    @NotNull(message = "Description" + NOT_NULL)
    @NotBlank(message = "Description" + NOT_BLANK)
    private String serviceDescription;

    private BigDecimal pricing; //minimum AUD$5

    private Set<AvailableDays> availableDays;

    private boolean available;
    
    private int startHour;

    private int closeMinute;

    private int closeHour;

    private int startMinute;

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

    private MultipartFile image1;

    private MultipartFile image2;

    private MultipartFile image3;
}
