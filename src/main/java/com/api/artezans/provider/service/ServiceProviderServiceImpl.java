package com.api.artezans.provider.service;

import com.api.artezans.authentication.services.AuthService;
import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.exceptions.TaskHubException;
import com.api.artezans.exceptions.UserNotFoundException;
import com.api.artezans.listings.data.models.Listing;
import com.api.artezans.listings.services.ServiceProviderListingService;
import com.api.artezans.multimedia.MultimediaService;
import com.api.artezans.notifications.app_notification.model.AppNotification;
import com.api.artezans.notifications.app_notification.service.AppNotificationService;
import com.api.artezans.payment.stripe.dto.CreateCustomerRequest;
import com.api.artezans.payment.stripe.services.StripeService;
import com.api.artezans.provider.data.dto.ServiceProviderRegistrationRequest;
import com.api.artezans.provider.data.dto.ServiceProviderUpdateRequest;
import com.api.artezans.provider.data.model.IdType;
import com.api.artezans.provider.data.model.ServiceProvider;
import com.api.artezans.provider.data.model.UserIdentity;
import com.api.artezans.provider.data.repository.ServiceProviderRepository;
import com.api.artezans.provider.data.repository.UserIdentityRepository;
import com.api.artezans.task.data.model.Task;
import com.api.artezans.task.service.ServiceProviderTaskService;
import com.api.artezans.users.models.Address;
import com.api.artezans.users.models.User;
import com.api.artezans.users.models.enums.AccountState;
import com.api.artezans.users.models.enums.Role;
import com.api.artezans.users.services.UserService;
import com.api.artezans.utils.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.api.artezans.utils.ApiResponse.apiResponse;
import static com.api.artezans.utils.TaskHubUtils.SERVICE_PROVIDER;
import static com.api.artezans.utils.TaskHubUtils.capitalized;

@Slf4j
@Service
@AllArgsConstructor
public class ServiceProviderServiceImpl implements ServiceProviderService {
    private final ServiceProviderListingService serviceProviderListingService;
    private final ServiceProviderTaskService serviceProviderTaskService;
    private final ServiceProviderRepository serviceProviderRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final AppNotificationService appNotificationService;
    private final AuthService authenticationService;
    private final MultimediaService multimediaService;
    private final PasswordEncoder passwordEncoder;
    private final StripeService stripeService;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ApiResponse register(
            ServiceProviderRegistrationRequest request, HttpServletRequest httpRequest) {
        String emailAddress = request.getEmailAddress();
        userService.validateExistence(emailAddress);
        request.setFirstName(capitalized(request.getFirstName()));
        request.setLastName(capitalized(request.getLastName()));
        ServiceProvider serviceProvider = ServiceProvider.builder()
                .user(User.builder()
                        .emailAddress(emailAddress)
                        .password(passwordEncoder.encode(request.getPassword()))
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .isEnabled(false)
                        .phoneNumber(request.getPhoneNumber())
                      //  .stripeId(registerServiceProviderOnStripe(request))   ////todo: will uncomment this when to go live
                        .roles(Collections.singleton(Role.SERVICE_PROVIDER))
                        .accountState(AccountState.NOT_VERIFIED)
                        .build())
                .build();
        boolean invalidIdRequest = request.getIdNumber() == null
                || request.getIdNumber().isEmpty() || request.getIdNumber().length() < 6;
        if (!invalidIdRequest) {
            serviceProvider.setUserIdentity(
                    UserIdentity.builder()
                            .idNumber(request.getIdNumber())
                            .build()
            );
        }
        try {
            userService.sendVerificationMail(serviceProvider.getUser());
            serviceProviderRepository.save(serviceProvider);
            return apiResponse("Successful! Please check your email to complete registration");
        } catch (RuntimeException ex) {
            throw new TaskHubException("Registration failed");
        }
    }

    private String registerServiceProviderOnStripe(
            @Valid ServiceProviderRegistrationRequest registrationRequest) {
        CreateCustomerRequest request = CreateCustomerRequest.builder()
                .name(registrationRequest.getFirstName() + " " + registrationRequest.getLastName())
                .email(registrationRequest.getEmailAddress())
                .phone(registrationRequest.getPhoneNumber())
                .description(SERVICE_PROVIDER)
                .build();
            return stripeService.createCustomer(request);
    }

    @Override
    public void save(ServiceProvider serviceProvider) {
        serviceProviderRepository.save(serviceProvider);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ApiResponse completeServiceProviderRegistration(String token, ServiceProviderUpdateRequest updateRequest) {
        User user = userService.getUserFromToken(token);
        ServiceProvider serviceProvider = findServiceProviderByUserEmailAddress(user.getEmailAddress());
        if (serviceProvider.getUser().getAddress() == null) {
            serviceProvider.getUser().setAddress(Address.builder()
                    .streetName(capitalized(updateRequest.getStreetName()))
                    .streetNumber(updateRequest.getStreetNumber())
                    .suburb(capitalized(updateRequest.getSuburb()))
                    .state(capitalized(updateRequest.getState()))
                    .postCode(updateRequest.getPostCode())
                    .unitNumber(updateRequest.getUnitNumber())
                    .build());
            UserIdentity userIdentity;
            if (serviceProvider.getUserIdentity() == null) {
                userIdentity = getNewUserIdentity(updateRequest);
            } else {
                userIdentity = getExistedUserIdentity(serviceProvider, updateRequest);
            }


//            userIdentity.setIdType(updateRequest.getIdType());
//            try {
//                userIdentity.setIdImage(multimediaService.upload(updateRequest.getIdImage()));
//            } catch (Exception e) {
//                throw new TaskHubException("an error occurred uploading your ID Image");
//            }
            serviceProvider.setUserIdentity(userIdentity);
            serviceProvider.getUser().setEnabled(true);
            serviceProviderRepository.save(serviceProvider);
        }
        return apiResponse("Your profile has been updated successfully");
    }

    private UserIdentity getNewUserIdentity(ServiceProviderUpdateRequest updateRequest) {
        boolean invalidIdNumber = updateRequest.getIdNumber() == null || updateRequest.getIdNumber().isEmpty();
        if (!invalidIdNumber) {
            UserIdentity userIdentity = new UserIdentity();
            userIdentity.setIdNumber(updateRequest.getIdNumber());
            userIdentity.setIdType(getIdType(updateRequest.getIdType()));
            try {
                userIdentity.setIdImage(multimediaService.upload(updateRequest.getIdImage()));
            } catch (Exception e) {
                throw new TaskHubException("an error occurred uploading your ID Image");
            }
            return userIdentity;
        }
        throw new TaskHubException("Id Number cannot be blank or empty");
    }

    private IdType getIdType(String idType) {
        IdType type = null;
        switch (idType) {
            case "Medicare Card" -> type = IdType.MEDICARE_CARD;
            case "International Passport" -> type = IdType.INTERNATIONAL_PASSPORT;
            case "Photo ID" -> type = IdType.PHOTO_ID;
            case "Driver's Licence" -> type = IdType.DRIVERS_LICENSE;
        }
        return type;
    }

    private UserIdentity getExistedUserIdentity(
            ServiceProvider serviceProvider, ServiceProviderUpdateRequest updateRequest) {
        UserIdentity userIdentity = userIdentityRepository.findByIdNumber(
                        serviceProvider.getUserIdentity().getIdNumber())
                .orElseThrow(TaskHubException::new);
        userIdentity.setIdType(getIdType(updateRequest.getIdType()));
        try {
            userIdentity.setIdImage(multimediaService.upload(updateRequest.getIdImage()));
        } catch (Exception e) {
            throw new TaskHubException("an error occurred uploading your ID Image");
        }
        return userIdentity;
    }

    @Override
    public ServiceProvider currentServiceProvider() {
        return findServiceProviderByUserEmailAddress(currentUser().getEmailAddress());
    }

    @Override
    public ApiResponse uploadProfilePicture(MultipartFile image) {
        return userService.uploadProfilePicture(image);
    }

    private ServiceProvider findServiceProviderByUserEmailAddress(String emailAddress) {
        return serviceProviderRepository.findServiceProviderByUserEmailAddress(emailAddress)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ApiResponse updateServiceProviderInfo(JsonPatch updatePayload) {
        ServiceProvider serviceProvider = currentServiceProvider();
        //Service Provider Object to node
        JsonNode node = objectMapper.convertValue(serviceProvider, JsonNode.class);
        try {
            //apply patch
            JsonNode updatedNode = updatePayload.apply(node);
            //node to   Service Provider Object
            ServiceProvider updatedServiceProvider = objectMapper.treeToValue(
                    updatedNode, ServiceProvider.class
            );
            serviceProviderRepository.save(updatedServiceProvider);
            return apiResponse("Updated successfully");
        } catch (JsonPatchException | JsonProcessingException e) {
            throw new TaskHubException(e.getMessage());
        }
    }

    @Override
    public boolean isExist(String email) {
        return serviceProviderRepository.existsByUserEmailAddressIgnoreCase(email);
    }


    @Override
    public List<Task> serviceProviderViewPeculiarTasks() {
        return serviceProviderTaskService
                .serviceProviderViewPeculiarTasks(getServiceNames());
    }

    @Override
    public List<AppNotification> serviceProviderNotifications() {
        return appNotificationService.findServiceProviderNotifications(
                currentServiceProvider().getUser().getId()
        );
    }

    private List<String> getServiceNames() {
        List<Listing> serviceProviderListings = serviceProviderListingService.serviceProviderListings(
                currentServiceProvider().getId()
        );
        return serviceProviderListings.stream()
                .map(Listing::getServiceName)
                .collect(Collectors.toList());
    }

    private User currentUser() {
        try {
            SecuredUser securedUser = (SecuredUser) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();
            return securedUser.getUser();
        } catch (Exception e) {
            throw new TaskHubException("User not authenticated");
        }
    }
}
