package com.api.artezans.provider.service;

import com.api.artezans.exceptions.ArtezanException;
import com.api.artezans.exceptions.UserNotFoundException;
import com.api.artezans.listings.data.models.Listing;
import com.api.artezans.listings.services.ServiceProviderListingService;
import com.api.artezans.multimedia.MultimediaService;
import com.api.artezans.notifications.app_notification.model.AppNotification;
import com.api.artezans.notifications.app_notification.service.AppNotificationService;
import com.api.artezans.payment.stripe.dto.CreateCustomerRequest;
import com.api.artezans.payment.stripe.services.StripeServiceImpl;
import com.api.artezans.provider.data.dto.ServiceProviderRegistrationRequest;
import com.api.artezans.provider.data.dto.ServiceProviderUpdateRequest;
import com.api.artezans.provider.data.model.enums.IdType;
import com.api.artezans.provider.data.model.ServiceProvider;
import com.api.artezans.provider.data.model.UserIdentity;
import com.api.artezans.provider.data.repository.ServiceProviderRepository;
import com.api.artezans.provider.data.repository.UserIdentityRepository;
import com.api.artezans.task.data.model.Task;
import com.api.artezans.task.service.ServiceProviderTaskService;
import com.api.artezans.users.dto.AddressMapper;
import com.api.artezans.users.dto.UserMailInfo;
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
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.api.artezans.utils.ApiResponse.apiResponse;
import static com.api.artezans.utils.ArtezanUtils.SERVICE_PROVIDER;
import static com.api.artezans.utils.ArtezanUtils.capitalized;

@Slf4j
@Service
@AllArgsConstructor
public class ServiceProviderServiceImpl implements ServiceProviderService {
    private final ServiceProviderListingService serviceProviderListingService;
    private final ServiceProviderTaskService serviceProviderTaskService;
    private final ServiceProviderRepository serviceProviderRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final AppNotificationService appNotificationService;
    private final MultimediaService multimediaService;
    private final PasswordEncoder passwordEncoder;
    private final StripeServiceImpl stripeServiceImpl;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final AddressMapper addressMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ApiResponse registerServiceProvider(ServiceProviderRegistrationRequest request) {

        String emailAddress = request.getEmailAddress();

        userService.validateUserExistenceByEmail(emailAddress);

        request.setFirstName(capitalized(request.getFirstName()));
        request.setLastName(capitalized(request.getLastName()));

        ServiceProvider serviceProvider = ServiceProvider.builder()
                .user(User.builder()
                        .emailAddress(emailAddress)
                        .password(passwordEncoder.encode(request.getPassword()))
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .enabled(false)
                        .phoneNumber(request.getPhoneNumber())
                        //  .stripeId(registerServiceProviderOnStripe(request))   ////todo: will uncomment this when to go live
                        .roles(Collections.singleton(Role.SERVICE_PROVIDER))
                        .accountState(AccountState.NOT_VERIFIED)
                        .build())
                .build();


        String idNumber = request.getIdNumber();
        if (StringUtils.hasText(idNumber) && idNumber.trim().length() >= 6) {
            serviceProvider.setUserIdentity(
                    UserIdentity.builder()
                            .idNumber(idNumber.trim())
                            .build()
            );
        }

        try {
            userService.sendVerificationMail(new UserMailInfo(serviceProvider.getUser()));
            serviceProviderRepository.save(serviceProvider);
            return apiResponse("Successful! Please check your email to complete registration");

        } catch (RuntimeException ex) {
            throw new ArtezanException("Registration failed");
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
        return stripeServiceImpl.createCustomer(request);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ApiResponse completeServiceProviderRegistration(String token, ServiceProviderUpdateRequest updateRequest) {

        User user = userService.getUserFromToken(token);
        ServiceProvider serviceProvider = findServiceProviderByUserEmailAddress(user.getEmailAddress());

        if (serviceProvider.getUser().getAddress() == null) {
            serviceProvider.getUser().setAddress(addressMapper.mapToAddress(updateRequest)); //give address
            UserIdentity userIdentity;
            if (serviceProvider.getUserIdentity() == null) {
                userIdentity = getNewUserIdentity(updateRequest);
            } else {
                userIdentity = getExistedUserIdentity(serviceProvider.getUserIdentity().getIdNumber(), updateRequest);
            }

            serviceProvider.setUserIdentity(userIdentity);
            serviceProvider.getUser().setEnabled(true);
            serviceProviderRepository.save(serviceProvider);
        }
        return apiResponse("Your profile has been updated successfully");
    }

    private UserIdentity getNewUserIdentity(ServiceProviderUpdateRequest updateRequest) {


        if (StringUtils.hasText(updateRequest.idNumber()) && updateRequest.idNumber().trim().length() >= 6) {

            UserIdentity userIdentity = UserIdentity.builder()
                    .idNumber(updateRequest.idNumber())
                    .idType(IdType.fromString(updateRequest.idType()))
                    .build();
            try {
                userIdentity.setIdImageUrl(multimediaService.upload(updateRequest.idImage()));
            } catch (Exception e) {
                throw new ArtezanException("an error occurred uploading your ID Image");
            }
            return userIdentity;
        }

        throw new ArtezanException("Id Number cannot be blank or empty");
    }

    private UserIdentity getExistedUserIdentity(String idNumber, ServiceProviderUpdateRequest updateRequest) {
        UserIdentity userIdentity = userIdentityRepository.findByIdNumber(idNumber)
                .orElseThrow(ArtezanException::new);
        
        userIdentity.setIdType(IdType.fromString(updateRequest.idType()));
        try {
            userIdentity.setIdImageUrl(multimediaService.upload(updateRequest.idImage()));
        } catch (Exception e) {
            throw new ArtezanException("an error occurred uploading your ID Image");
        }
        return userIdentity;
    }

    @Override
    public ServiceProvider currentServiceProvider(String emailAddress) {
        return findServiceProviderByUserEmailAddress(emailAddress);
    }

    @Override
    public ApiResponse uploadProfilePicture(MultipartFile image, User user) {
        return userService.uploadProfilePicture(image, user);
    }

    private ServiceProvider findServiceProviderByUserEmailAddress(String emailAddress) {
        return serviceProviderRepository.findServiceProviderByUserEmailAddress(emailAddress)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ApiResponse updateServiceProviderInfo(JsonPatch updatePayload, String emailAddress) {
        ServiceProvider serviceProvider = currentServiceProvider(emailAddress);
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
            throw new ArtezanException(e.getMessage());
        }
    }

    @Override
    public boolean validateServiceProviderExistence(String email) {
        return serviceProviderRepository.existsByUserEmailAddressIgnoreCase(email);
    }

    @Override
    public List<Task> serviceProviderViewPeculiarTasks(String emailAddress) {
        return serviceProviderTaskService
                .serviceProviderViewPeculiarTasks(getServiceNames(emailAddress));
    }

    @Override
    public List<AppNotification> serviceProviderNotifications(String emailAddress) {
        return appNotificationService.findServiceProviderNotifications(
                currentServiceProvider(emailAddress).getUser().getId()
        );
    }

    private List<String> getServiceNames(String emailAddress) {
        return serviceProviderListingService
                .serviceProviderListings(currentServiceProvider(emailAddress).getId())
                .stream()
                .map(Listing::getServiceName)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
