package com.api.artezans.listings.services;

import com.api.artezans.exceptions.TaskHubException;
import com.api.artezans.exceptions.UserNotAuthorizedException;
import com.api.artezans.listings.data.dtos.ListingRequest;
import com.api.artezans.listings.data.dtos.LocationFilter;
import com.api.artezans.listings.data.models.Listing;
import com.api.artezans.listings.data.repositories.ListingRepository;
import com.api.artezans.multimedia.MultimediaService;
import com.api.artezans.payment.stripe.dto.ProductRequest;
import com.api.artezans.payment.stripe.services.StripeService;
import com.api.artezans.provider.data.model.ServiceProvider;
import com.api.artezans.provider.service.ServiceProviderService;
import com.api.artezans.users.models.Address;
import com.api.artezans.utils.ApiResponse;
import com.api.artezans.utils.Paginate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static com.api.artezans.utils.ApiResponse.apiResponse;
import static com.api.artezans.utils.TaskHubUtils.*;

@Slf4j
@Service
@AllArgsConstructor
public class ListingServiceImpl implements ListingService {
    private final ServiceProviderService serviceProviderService;
    private final MultimediaService multimediaService;
    private final ListingRepository listingRepository;
    private final StripeService stripeService;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ApiResponse createListing(@Valid ListingRequest request) {
        request.setServiceName(capitalized(request.getServiceName()));
        request.setServiceDescription(capitalized(request.getServiceDescription()));
        Timing timing = getTime(request);
        Listing listing = Listing.builder()
                .businessName(request.getBusinessName())
                .serviceCategory(request.getServiceCategory().toUpperCase())
                .serviceName(request.getServiceName())
                .serviceDescription(request.getServiceDescription())
                .availableDays(request.getAvailableDays())
                .available(request.isAvailable())
                .address(getAddress(request))
                .availableFrom(timing.startTime)
                .availableTo(timing.endTime)
                .pricing(getPrice(request))
                .build();
        ServiceProvider serviceProvider = serviceProviderService.currentServiceProvider();
       // listing.setStripeId(registerListingAsProductOnStripe(request, serviceProvider));  ////todo: Will uncomment this when to go live
        listing.setServiceProvider(serviceProvider);
        listing.setBusinessPictures(uploadBusinessImages(request));
        listingRepository.save(listing);
        return apiResponse("Listing created successfully");
    }

    private String registerListingAsProductOnStripe( // TODO: 11-Nov-23 to be used when gone live 
            @Valid ListingRequest listingRequest, ServiceProvider serviceProvider) {
        ProductRequest request = ProductRequest.builder()
                .serviceName(listingRequest.getServiceName())
                .serviceDescription(listingRequest.getServiceDescription())
                .serviceProviderStripeId(serviceProvider.getUser().getStripeId())
                .isActive(listingRequest.isAvailable())
                .servicePricePerUnit(listingRequest.getPricing().longValue())
                .build();
        return stripeService.createProduct(request);
    }

    public record Timing(LocalTime startTime, LocalTime endTime) {
    }

    private Timing getTime(ListingRequest request) {
        LocalTime availableFrom = LocalTime.of(
                request.getStartHour(),
                request.getStartMinute()
        );
        LocalTime availableTo = LocalTime.of(
                request.getCloseHour(),
                request.getCloseMinute()
        );
        if (availableFrom.isAfter(availableTo)) {
            throw new TaskHubException("Time is incoherent");
        }
        return new Timing(availableFrom, availableTo);
    }

    private BigDecimal getPrice(ListingRequest request) {
        return request.getPricing().compareTo(BigDecimal.valueOf(5)) > 0
                ? request.getPricing()
                : BigDecimal.valueOf(5);
    }

    private Address getAddress(ListingRequest request) {
        return Address.builder()
                .streetNumber(request.getStreetNumber())
                .streetName(capitalized(request.getStreetName()))
                .suburb(capitalized(request.getSuburb()))
                .state(capitalized(request.getState()))
                .postCode(request.getPostCode())
                .unitNumber(request.getUnitNumber())
                .build();
    }


    @Override
    public List<String> viewAllListingPictures(Long listingId) {
        return findListingById(listingId).getBusinessPictures();
        // return businessPictureRepository.findAllByListing_Id(listingId);
    }

    @Override
    public Paginate<Listing> getAllListings(int pageNUmber) {
        log.info("{}", listingRepository.findAll());
        Page<Listing> listings = listingRepository.findAll(
                PageRequest.of(
                        pageNUmber < 1 ? 0 : pageNUmber - 1,
                        MAX_PER_PAGE
                )
        );
        Type paginatedListings = new TypeToken<Paginate<Listing>>() {
        }.getType();
        return modelMapper.map(listings, paginatedListings);
    }


    @Override
    public Paginate<Listing> getAllServiceProviderListings(int pageNUmber) {
        Pageable pageable = PageRequest.of(
                pageNUmber < 1 ? 0 : pageNUmber - 1,
                MAX_PER_PAGE);
        List<Listing> serviceProviderListings = listingRepository.findAllByServiceProvider_Id(
                serviceProviderService.currentServiceProvider().getId()
        );
        Page<Listing> listings =
                new PageImpl<>(serviceProviderListings, pageable, serviceProviderListings.size());
        Type paginatedListings = new TypeToken<Paginate<Listing>>() {
        }.getType();
        return modelMapper.map(listings, paginatedListings);
    }

    @Override
    public Paginate<Listing> getAllUndeletedListings(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber < 1 ? 0 : pageNumber - 1, MAX_PER_PAGE);
        List<Listing> undeletedListings = listingRepository.findAllUndeletedListings();
        Page<Listing> listings = new PageImpl<>(
                undeletedListings, pageable, undeletedListings.size()
        );
        Type paginatedListings = new TypeToken<Paginate<Listing>>() {
        }.getType();
        return modelMapper.map(listings, paginatedListings);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ApiResponse updateListing(Long listingId, JsonPatch jsonPatch) {
        Listing listing = findListingById(listingId);
        if (listing.getServiceProvider().equals(serviceProviderService.currentServiceProvider())) {
            JsonNode jsonNode = objectMapper.convertValue(listing, JsonNode.class);
            ServiceProvider serviceProvider = listing.getServiceProvider();
            try {
                JsonNode updateNode = jsonPatch.apply(jsonNode);
                Listing updatedListing = objectMapper.treeToValue(updateNode, Listing.class);
                updatedListing.setServiceProvider(serviceProvider);
                listingRepository.save(updatedListing);
                return apiResponse("Updated successfully");
            } catch (JsonPatchException | JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        throw new UserNotAuthorizedException();
    }

    @Override
    public Listing userFindsListingById(Long listingId) {
        return listingRepository.findActiveListingById(listingId)
                .orElseThrow(() -> new TaskHubException("Listing does not exist"));
    }

    @Override
    public Listing adminFindsListingById(Long listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new TaskHubException("Listing does not exist"));
    }

    @Override
    public ApiResponse deleteListing(Long listingId) {
        Listing listing = adminFindsListingById(listingId);
        if (listing.getServiceProvider().equals(serviceProviderService.currentServiceProvider())) {
            listing.setDeleted(true);
            listingRepository.save(listing);
            return apiResponse("Listing with id " + listing.getId() + " is deleted successfully");
        }
        throw new UserNotAuthorizedException();
    }

    @Override
    public List<Listing> findListingByServiceName(String serviceName) {
        return listingRepository.findByServiceNameIgnoreCase(serviceName);
        // if (listings.isEmpty()) throw new TaskHubException(NO_LISTINGS);
    }

    @Override
    public ApiResponse findServicesFilterByLocation(LocationFilter filterBy) {
        List<Listing> listings = listingRepository.findByLocation(filterBy.getServiceName(), filterBy.getLocation());
        if (listings.isEmpty()) throw new TaskHubException(NO_LISTINGS);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("These are the services found, when filtered by ").append(filterBy.getServiceName());
        if (!filterBy.getLocation().isEmpty()) {
            stringBuilder.append(" and ").append(filterBy.getLocation());
        }

        return apiResponse(listings, stringBuilder.toString());
    }

    private void addListing(Address address, List<Listing> listings) {
        listings.add(listingRepository.findActiveListingByAddressId(address.getId()).get());
    }


    @Override
    public ApiResponse findByServiceNameAndAddressState(String serviceName, String location) {
        List<Listing> listings = listingRepository.findByLocation(serviceName, location);
        if (listings.isEmpty()) {
            throw new TaskHubException(NO_LISTINGS);
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("These are the services found, when filtered by ").append(serviceName);
        if (!location.isEmpty()) {
            stringBuilder.append(" and ").append(location);
        }

        return apiResponse(listings, stringBuilder.toString());
    }

    private Listing findListingById(Long listingId) {
        return listingRepository.findActiveListingById(listingId)
                .orElseThrow(() -> new TaskHubException("Listing could not be found"));
    }

    private List<String> uploadBusinessImages(ListingRequest request) {
        try {
            return List.of(
                    multimediaService.upload(request.getImage1()),
                    multimediaService.upload(request.getImage2()),
                    multimediaService.upload(request.getImage3())
            );
        } catch (Exception e) {
            throw new TaskHubException("Error uploading business pictures");
        }
    }
}