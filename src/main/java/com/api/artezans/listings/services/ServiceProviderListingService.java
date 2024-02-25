package com.api.artezans.listings.services;

import com.api.artezans.listings.data.models.Listing;
import com.api.artezans.listings.data.repositories.ListingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ServiceProviderListingService {
    private final ListingRepository listingRepository;

    public List<Listing> serviceProviderListings(Long serviceProviderId) {
        return listingRepository.findAllByServiceProvider_Id(serviceProviderId);
    }
}
