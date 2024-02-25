package com.api.artezans.listings.controller;

import com.github.fge.jsonpatch.JsonPatch;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import task.hub.user.listings.data.dtos.ListingRequest;
import task.hub.user.listings.data.dtos.LocationFilter;
import task.hub.user.listings.data.models.Listing;
import task.hub.user.listings.services.ListingService;
import task.hub.user.utils.ApiResponse;
import task.hub.user.utils.Paginate;

import java.util.List;

@Component
@AllArgsConstructor
public class ListingController {
    private final ListingService listingService;

    public ApiResponse createListing(@Valid ListingRequest request) {
        return listingService.createListing(request);
    }

    public List<String> viewAllListingPictures(Long listingId) {
        return listingService.viewAllListingPictures(listingId);
    }
    public Paginate<Listing> getAllListings(int pageNumber) {
        return listingService.getAllListings(pageNumber);
    }

    public Paginate<Listing> getAllServiceProviderListings(int pageNumber) {
        return listingService.getAllServiceProviderListings(pageNumber);
    }
    public Paginate<Listing> getAllUndeletedListings(int pageNumber) {
        return listingService.getAllUndeletedListings(pageNumber);
    }

    public ApiResponse updateListing(Long listingId, JsonPatch jsonPatch) {
        return listingService.updateListing(listingId, jsonPatch);
    }
    public ApiResponse deleteListing(Long listingId) {
        return listingService.deleteListing(listingId);
    }
    public Listing adminFindsListingById(Long listing) {
        return listingService.adminFindsListingById(listing);
    }
    public Listing userFindsListingById(Long listing) {
        return listingService.userFindsListingById(listing);
    }
    public List<Listing> findAllListingsByServiceName(String serviceName){
        return listingService.findListingByServiceName(serviceName);
    }
    public ApiResponse findServicesFilterByLocation(LocationFilter filterBy){
        return listingService.findServicesFilterByLocation( filterBy);
    }
}
