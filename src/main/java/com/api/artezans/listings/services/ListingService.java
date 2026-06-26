package com.api.artezans.listings.services;

import com.api.artezans.listings.data.dtos.ListingRequest;
import com.api.artezans.listings.data.dtos.LocationFilter;
import com.api.artezans.listings.data.models.Listing;
import com.api.artezans.utils.ApiResponse;
import com.api.artezans.utils.Paginate;
import com.github.fge.jsonpatch.JsonPatch;

import java.util.List;

public interface ListingService {
    ApiResponse createListing(ListingRequest request, String emailAddress);

    List<String> viewAllListingPictures(Long listingId);

    Paginate<Listing> getAllListings(int pageNUmber);

    Paginate<Listing> getAllServiceProviderListings(int pageNumber, String emailAddress);

    Paginate<Listing> getAllUndeletedListings(int pageNumber);

    ApiResponse updateListing(Long listingId, JsonPatch jsonPatch, String email);

    Listing userFindsListingById(Long listingId);

    Listing adminFindsListingById(Long listingId);

    ApiResponse deleteListing(Long listingId, String emailAddress);

    List<Listing> findAllListingsByServiceName(String serviceName);

    ApiResponse findServicesFilterByLocation(LocationFilter filterBy);

    ApiResponse findByServiceNameAndAddressState(String serviceName, String state);
}