package com.api.artezans.gateway.listing;

import com.api.artezans.listings.controller.ListingController;
import com.api.artezans.listings.data.dtos.ListingRequest;
import com.api.artezans.listings.data.dtos.LocationFilter;
import com.api.artezans.listings.data.models.Listing;
import com.api.artezans.utils.ApiResponse;
import com.api.artezans.utils.Paginate;
import com.github.fge.jsonpatch.JsonPatch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.api.artezans.gateway.listing.ListingUtil.*;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@AllArgsConstructor
@Tag(name = "Listing Controller")
@RequestMapping("api/v1/listing")
public class ListingGateway {
    private final ListingController listingController;

    //@PreAuthorize("hasAuthority('SERVICE_PROVIDER')")
    @PostMapping(value = "/create-listing", consumes = MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = CREATE_SUM, description = CREATE_DESC, operationId = CREATE_OP_ID)
    public ResponseEntity<ApiResponse> createListing(@ModelAttribute @Valid ListingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(listingController.createListing(request));
    }

    @GetMapping("listing-images/{listingId}")
    @Operation(summary = IMAGES_SUM, description = IMAGES_DESC, operationId = IMAGES_OP_ID)
    public ResponseEntity<List<String>> viewAllListingPictures(@PathVariable Long listingId) {
        return ResponseEntity.ok(
                listingController.viewAllListingPictures(listingId)
        );
    }

    @GetMapping("listings/{pageNumber}")
    @PreAuthorize("hasAuthority('SERVICE_PROVIDER')")
    @Operation(summary = SP_LIST_SUM, description = SP_LIST_DESC, operationId = SP_LIST_OP_ID)
    public ResponseEntity<Paginate<Listing>> getAllServiceProviderListings(@PathVariable int pageNumber) {
        return ResponseEntity.ok(
                listingController.getAllServiceProviderListings(pageNumber)
        );
    }

    @GetMapping("undeleted/{pageNumber}")
    // @PreAuthorize("hasAuthority('SERVICE_PROVIDER')")
    @Operation(summary = UNDELETED_SUM, description = UNDELETED_DESC, operationId = UNDELETED_OP_ID)
    public ResponseEntity<Paginate<Listing>> getAllUndeletedListings(@PathVariable int pageNumber) {
        return ResponseEntity.ok(
                listingController.getAllUndeletedListings(pageNumber)
        );
    }

    @PatchMapping("update-listing/{listingId}")
    @PreAuthorize("hasAuthority('SERVICE_PROVIDER')")
    @Operation(summary = UPDATE_SUM, description = UPDATE_DESC, operationId = UPDATE_OP_ID)
    public ResponseEntity<ApiResponse> updateListing(
            @PathVariable Long listingId, @RequestBody JsonPatch jsonPatch) {
        return ResponseEntity.ok(
                listingController.updateListing(listingId, jsonPatch)
        );
    }

    @DeleteMapping("delete-listing/{listingId}")
    @PreAuthorize("hasAuthority('SERVICE_PROVIDER')")
    @Operation(summary = DELETE_SUM, description = DELETE_DESC, operationId = DELETE_OP_ID)
    public ResponseEntity<ApiResponse> deleteListing(@PathVariable Long listingId) {
        return ResponseEntity.ok(
                listingController.deleteListing(listingId)
        );
    }

    @GetMapping("by-id/{listingId}")
    @Operation(summary = BY_ID_SUM, description = BY_ID_DESC, operationId = BY_ID_OP_ID)
    public ResponseEntity<Listing> userFindsListingById(@PathVariable Long listingId) {
        return ResponseEntity.ok(
                listingController.userFindsListingById(listingId)
        );
    }


    @GetMapping("/by-service-name/{serviceName}")
    @Operation(summary = SERVICE_NAME_SUM, description = SERVICE_NAME_DESC, operationId = SERVICE_NAME_OP_ID)
    public ResponseEntity<List<Listing>> findListingsByServiceName(@PathVariable String serviceName) {
        return ResponseEntity.ok(
                listingController.findAllListingsByServiceName(serviceName));
    }


    @GetMapping("/by-location")
    @Operation(summary = BY_LOC_SUM, description = BY_LOC_DESC, operationId = BY_LOC_OP_ID)
    public ResponseEntity<ApiResponse> getListingsByLocation(@ParameterObject LocationFilter filterBy) {
        return ResponseEntity.ok(
                listingController.findServicesFilterByLocation(filterBy)
        );
    }
}
