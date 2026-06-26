package com.api.artezans.users.dto;

public record AddressDTO(
        String streetNumber,
        String streetName,
        String unitNumber,
        String city,
        String state,
        String postCode
) {
}