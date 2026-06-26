package com.api.artezans.listings.services;

import com.api.artezans.exceptions.ArtezanException;
import com.api.artezans.exceptions.UserNotAuthorizedException;
import com.api.artezans.listings.data.dtos.ListingRequest;
import com.api.artezans.listings.data.dtos.LocationFilter;
import com.api.artezans.listings.data.models.Listing;
import com.api.artezans.listings.data.models.Timing;
import com.api.artezans.listings.data.repositories.ListingRepository;
import com.api.artezans.multimedia.MultimediaService;
import com.api.artezans.payment.stripe.dto.ProductRequest;
import com.api.artezans.payment.stripe.services.StripeServiceImpl;
import com.api.artezans.provider.data.model.ServiceProvider;
import com.api.artezans.provider.service.ServiceProviderService;
import com.api.artezans.users.dto.AddressMapper;
import com.api.artezans.utils.ApiResponse;
import com.api.artezans.utils.Paginate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static com.api.artezans.utils.ApiResponse.apiResponse;
import static com.api.artezans.utils.ArtezanUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {

    private final ServiceProviderService serviceProviderService;
    private final MultimediaService multimediaService;
    private final ListingRepository listingRepository;
    private final StripeServiceImpl stripeServiceImpl;
    private final ObjectMapper objectMapper;
    private final AddressMapper addressMapper;


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ApiResponse createListing(ListingRequest request, String emailAddress) {
        request.setServiceName(capitalized(request.getServiceName()));
        request.setServiceDescription(capitalized(request.getServiceDescription()));


        Timing timing = buildTiming(request);
        List<String> imageUrls = multimediaService.upload(request.getImages());
        ServiceProvider serviceProvider = serviceProviderService.currentServiceProvider(emailAddress);

        // TODO: Uncomment when going live — registers service as a Stripe product
//        String stripeId = registerListingOnStripe(request, serviceProvider);


        Listing listing = listingRepository.save(
                Listing.builder()
                        .businessName(request.getBusinessName())
                        .serviceCategory(request.getServiceCategory().toUpperCase())
                        .serviceName(request.getServiceName())
                        .serviceDescription(request.getServiceDescription())
                        .availableDays(request.getAvailableDays())
                        .available(request.isAvailable())
                        .address(addressMapper.mapToAddress(request))
                        .availableFrom(timing.startTime())
                        .availableTo(timing.endTime())
                        .pricing(minimumPrice(request))
                        .serviceProvider(serviceProvider)
                        .businessPictures(imageUrls)
//                      .stripeId(stripeId)
                        .build()
        );

        log.info("Listing '{}' created for provider: }", listing.getServiceName());
        return apiResponse("Listing created successfully");
    }

    @Override
    public Paginate<Listing> getAllListings(int pageNumber) {
        Pageable pageable = pageableOf(pageNumber);
        Page<Listing> page = listingRepository.findAll(pageable);
        return Paginate.fromPage(page);
    }

    @Override
    public Paginate<Listing> getAllServiceProviderListings(int pageNumber, String emailAddress) {
        Long providerId = serviceProviderService.currentServiceProvider(emailAddress).getId();
        Pageable pageable = pageableOf(pageNumber);

        List<Listing> listings = listingRepository.findAllByServiceProvider_Id(providerId);
        return Paginate.fromPage(new org.springframework.data.domain.PageImpl<>(listings, pageable, listings.size()));
    }

    @Override
    public Paginate<Listing> getAllUndeletedListings(int pageNumber) {
        Pageable pageable = pageableOf(pageNumber);

        List<Listing> listings = listingRepository.findAllUndeletedListings();
        return Paginate.fromPage(new org.springframework.data.domain.PageImpl<>(listings, pageable, listings.size()));
    }

    @Override
    public List<String> viewAllListingPictures(Long listingId) {
        return findActiveListingById(listingId).getBusinessPictures();
    }

    @Override
    public Listing userFindsListingById(Long listingId) {
        return findActiveListingById(listingId);
    }

    @Override
    public Listing adminFindsListingById(Long listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new ArtezanException("Listing does not exist"));
    }

    @Override
    public List<Listing> findAllListingsByServiceName(String serviceName) {
        return listingRepository.findByServiceNameIgnoreCase(serviceName);
    }

    /**
     * Finds listings filtered by service name and optionally by location.
     * Delegates to {@link #findByServiceNameAndAddressState} to avoid code duplication.
     */
    @Override
    public ApiResponse findServicesFilterByLocation(LocationFilter filterBy) {
        return findByServiceNameAndAddressState(filterBy.serviceName(), filterBy.location());
    }

    @Override
    public ApiResponse findByServiceNameAndAddressState(String serviceName, String location) {
        List<Listing> listings = listingRepository.findByLocation(serviceName, location);
        if (listings.isEmpty()) {
            throw new ArtezanException(NO_LISTINGS);
        }

        String message = buildLocationFilterMessage(serviceName, location);
        return apiResponse(listings, message);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ApiResponse updateListing(Long listingId, JsonPatch jsonPatch, String email) {
        Listing listing = findActiveListingById(listingId);
        ServiceProvider currentProvider = serviceProviderService.currentServiceProvider(email);

        if (!listing.getServiceProvider().equals(currentProvider)) {
            throw new UserNotAuthorizedException();
        }

        ServiceProvider originalProvider = listing.getServiceProvider();
        try {
            JsonNode jsonNode = objectMapper.convertValue(listing, JsonNode.class);
            JsonNode updatedNode = jsonPatch.apply(jsonNode);
            Listing updatedListing = objectMapper.treeToValue(updatedNode, Listing.class);
            updatedListing.setServiceProvider(originalProvider);
            listingRepository.save(updatedListing);

            return apiResponse("Updated successfully");
        } catch (JsonPatchException e) {

            log.error("Failed to apply JSON patch to listing [id={}]: {}", listingId, e.getMessage());
            throw new ArtezanException("Invalid patch operation: " + e.getMessage());
        } catch (JsonProcessingException e) {

            log.error("JSON processing error while updating listing [id={}]: {}", listingId, e.getMessage());
            throw new ArtezanException("Failed to process listing update");
        }
    }


    @Override
    public ApiResponse deleteListing(Long listingId, String emailAddress) {
        Listing listing = adminFindsListingById(listingId);
        ServiceProvider currentProvider = serviceProviderService.currentServiceProvider(emailAddress);

        if (!listing.getServiceProvider().equals(currentProvider)) {
            throw new UserNotAuthorizedException();
        }

        listing.setDeleted(true);
        listingRepository.save(listing);
        log.info("Listing [id={}] soft-deleted by provider [email={}]", listing.getId(), emailAddress);
        return apiResponse("Listing with id " + listing.getId() + " deleted successfully");
    }


    /**
     * Returns a zero-based {@link Pageable}, treating any page number < 1 as page 1.
     */
    private Pageable pageableOf(int pageNumber) {
        return PageRequest.of(pageNumber < 1 ? 0 : pageNumber - 1, MAX_PER_PAGE);
    }

    /**
     * Looks up an active (non-deleted) listing, or throws {@link ArtezanException}.
     */
    private Listing findActiveListingById(Long listingId) {
        return listingRepository.findActiveListingById(listingId)
                .orElseThrow(() -> new ArtezanException("Listing could not be found"));
    }

    /**
     * Builds a user-friendly message describing the location filter applied.
     */
    private String buildLocationFilterMessage(String serviceName, String location) {
        StringBuilder sb = new StringBuilder("These are the services found, when filtered by ").append(serviceName);
        if (location != null && !location.isBlank()) {
            sb.append(" and ").append(location);
        }
        return sb.toString();
    }

    /**
     * Parses start/end times from the request, validating coherence.
     */
    private Timing buildTiming(ListingRequest request) {
        LocalTime from = LocalTime.of(request.getStartHour(), request.getStartMinute());
        LocalTime to = LocalTime.of(request.getCloseHour(), request.getCloseMinute());

        if (from.isAfter(to)) {
            throw new ArtezanException("Opening time cannot be after closing time");
        }
        return new Timing(from, to);
    }

    /**
     * Enforces a minimum price floor of AUD $5.
     */
    private BigDecimal minimumPrice(ListingRequest request) {
        BigDecimal floor = BigDecimal.valueOf(5);
        return (request.getPricing() != null && request.getPricing().compareTo(floor) > 0)
                ? request.getPricing()
                : floor;
    }


    /**
     * Uploads listing images and returns the resulting URLs.
     */
    private List<String> uploadBusinessImages(ListingRequest request) {
        try {
            return multimediaService.upload(request.getImages());
        } catch (Exception e) {
            log.error("Failed to upload business pictures: {}", e.getMessage());
            throw new ArtezanException("Error uploading business pictures");
        }
    }

    /**
     * Registers a listing as a product on Stripe.
     * TODO: Uncomment in {@link #createListing} when going live.
     */
    @SuppressWarnings("unused")
    private String registerListingOnStripe(ListingRequest listingRequest, ServiceProvider serviceProvider) {
        ProductRequest request = ProductRequest.builder()
                .serviceName(listingRequest.getServiceName())
                .serviceDescription(listingRequest.getServiceDescription())
                .serviceProviderStripeId(serviceProvider.getUser().getStripeId())
                .isActive(listingRequest.isAvailable())
                .servicePricePerUnit(listingRequest.getPricing().longValue())
                .build();
        return stripeServiceImpl.createProduct(request);
    }

    /** Immutable value type holding parsed availability hours. */
//    public record Timing(LocalTime startTime, LocalTime endTime) {}
}