package com.api.artezans.customer.services;

import com.api.artezans.customer.data.dto.request.CustomerRegistrationRequest;
import com.api.artezans.customer.data.dto.request.CustomerUpdateRequest;
import com.api.artezans.customer.data.model.Customer;
import com.api.artezans.customer.data.repository.CustomerRepository;
import com.api.artezans.exceptions.TaskHubException;
import com.api.artezans.exceptions.UserNotFoundException;
import com.api.artezans.payment.stripe.dto.CreateCustomerRequest;
import com.api.artezans.payment.stripe.services.StripeService;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

import static com.api.artezans.utils.ApiResponse.apiResponse;
import static com.api.artezans.utils.TaskHubUtils.CUSTOMER;
import static com.api.artezans.utils.TaskHubUtils.capitalized;


@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final UserService userService;
    private final StripeService stripeService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ApiResponse register(
            CustomerRegistrationRequest registrationRequest)  {
        String emailAddress = registrationRequest.getEmailAddress();
        userService.validateExistence(emailAddress);
        registrationRequest.setFirstName(capitalized(registrationRequest.getFirstName()));
        registrationRequest.setLastName(capitalized(registrationRequest.getLastName()));
        Customer customer = Customer.builder()
                .user(User.builder()
                        .firstName(registrationRequest.getFirstName())
                        .lastName(registrationRequest.getLastName())
                        .phoneNumber(registrationRequest.getPhoneNumber())
                        //  .stripeId(registerCustomerOnStripe(registrationRequest)) ////todo: will uncomment this when to go live
                        .emailAddress(emailAddress)
                        .isEnabled(false)
                        .password(passwordEncoder.encode(registrationRequest.getPassword()))
                        .roles(Collections.singleton(Role.CUSTOMER))
                        .accountState(AccountState.NOT_VERIFIED)
                        .build())
                .build();
        try {
            userService.sendVerificationMail(customer.getUser());
            customerRepository.save(customer);
            return apiResponse("Successful! Please check your email to complete registration");
        } catch (RuntimeException ex) {
            throw new TaskHubException("Registration failed!");
        }
    }

    private String registerCustomerOnStripe(@Valid CustomerRegistrationRequest registrationRequest) {
        CreateCustomerRequest request = CreateCustomerRequest.builder()
                .name(registrationRequest.getFirstName() + " " + registrationRequest.getLastName())
                .email(registrationRequest.getEmailAddress())
                .phone(registrationRequest.getPhoneNumber())
                .description(CUSTOMER)
                .build();
        try {
            return stripeService.createCustomer(request);
        } catch (Exception e) {
            throw new TaskHubException(e.getMessage());
        }
    }

    @Override
    public Customer currentCustomer(User user) {
        return findCustomerByUserEmailAddress(user.getEmailAddress());
    }

    public Customer findCustomerByUserEmailAddress(String email) {
        return customerRepository.findCustomerByUserEmailAddress(email)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    public ApiResponse customerCompleteRegistration(String token, CustomerUpdateRequest updateRequest) {
        Customer customer = findCustomerByUserEmailAddress(
                userService.getUserFromToken(token).getEmailAddress());
        if (customer.getUser().getAddress() == null) {
            customer.getUser().setAddress(getAddress(updateRequest));
            customer.getUser().setEnabled(true);
            customerRepository.save(customer);
        }
        return apiResponse("Your profile has been updated successfully");
    }

    private static Address getAddress(CustomerUpdateRequest updateRequest) {
        return Address.builder()
                .streetName(capitalized(updateRequest.getStreetName()))
                .streetNumber(updateRequest.getStreetNumber())
                .suburb(capitalized(updateRequest.getSuburb()))
                .state(capitalized(updateRequest.getState()))
                .postCode(updateRequest.getPostCode())
                .unitNumber(updateRequest.getUnitNumber())
                .build();
    }

    @Override
    public ApiResponse uploadProfilePicture(MultipartFile image) {
        return userService.uploadProfilePicture(image);
    }

    @Override
    public ApiResponse updateCustomerInfo(JsonPatch updatePayload, User user) {
        Customer customer = currentCustomer(user);
        //Passenger Object to node
        JsonNode node = objectMapper.convertValue(customer, JsonNode.class);
        try {
            //apply patch
            JsonNode updatedNode = updatePayload.apply(node);
            //node to Customer Object
            Customer updatedCustomer = objectMapper.treeToValue(updatedNode, Customer.class);
            customerRepository.save(updatedCustomer);
            return apiResponse("Updated successfully");
        } catch (JsonPatchException | JsonProcessingException e) {
            throw new TaskHubException(e.getMessage());
        }
    }
}