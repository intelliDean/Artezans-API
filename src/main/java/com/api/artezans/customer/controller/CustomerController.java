package com.api.artezans.customer.controller;

import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.customer.data.dto.request.CustomerRegistrationRequest;
import com.api.artezans.customer.data.dto.request.CustomerUpdateRequest;
import com.api.artezans.customer.services.CustomerService;
import com.api.artezans.utils.ApiResponse;
import com.github.fge.jsonpatch.JsonPatch;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@AllArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    public ApiResponse register(@Valid CustomerRegistrationRequest registrationRequest ) {
        return customerService.register(registrationRequest);
    }
    public ApiResponse updateCustomerProfile(String token, CustomerUpdateRequest updateRequest) {
        return customerService.customerCompleteRegistration(token, updateRequest);
    }
    public ApiResponse uploadProfileImage(MultipartFile image) {
        return customerService.uploadProfilePicture(image);
    }
    public ApiResponse updateCustomerInfo (JsonPatch updatePayload, SecuredUser securedUser) {
        return customerService.updateCustomerInfo(updatePayload, securedUser.getUser());
    }
}