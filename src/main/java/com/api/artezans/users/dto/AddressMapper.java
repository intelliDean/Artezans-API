package com.api.artezans.users.dto;

import com.api.artezans.customer.data.dto.request.CustomerUpdateRequest;
import com.api.artezans.listings.data.dtos.ListingRequest;
import com.api.artezans.provider.data.dto.ServiceProviderUpdateRequest;
import com.api.artezans.users.models.Address;
import com.api.artezans.users.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)// makes it a Spring bean
public interface AddressMapper {

    @Named("mapToAddress")
    Address mapToAddress(CustomerUpdateRequest updateRequest);

    @Named("mapToAddress")
    Address mapToAddress(ServiceProviderUpdateRequest updateRequest);

    @Named("mapToAddress")
    Address mapToAddress(ListingRequest request);
}