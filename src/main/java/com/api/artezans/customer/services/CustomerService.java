package com.api.artezans.customer.services;


import com.api.artezans.customer.data.dto.request.CustomerRegistrationRequest;
import com.api.artezans.customer.data.dto.request.CustomerUpdateRequest;
import com.api.artezans.customer.data.model.Customer;
import com.api.artezans.users.models.User;
import com.api.artezans.utils.ApiResponse;
import com.github.fge.jsonpatch.JsonPatch;
import org.springframework.web.multipart.MultipartFile;


public interface CustomerService {
    ApiResponse register(CustomerRegistrationRequest registrationRequest);
    Customer currentCustomer(User user);
    ApiResponse customerCompleteRegistration(String token, CustomerUpdateRequest customerRequest);
    ApiResponse uploadProfilePicture(MultipartFile image);
    ApiResponse updateCustomerInfo (JsonPatch updatePayload, User user);
}