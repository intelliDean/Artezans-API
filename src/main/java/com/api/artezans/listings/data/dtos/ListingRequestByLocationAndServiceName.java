package com.api.artezans.listings.data.dtos;


public record ListingRequestByLocationAndServiceName (
        String serviceName,
        String state
) {}