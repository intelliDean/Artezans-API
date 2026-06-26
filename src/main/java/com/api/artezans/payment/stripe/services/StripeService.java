package com.api.artezans.payment.stripe.services;

import com.api.artezans.payment.stripe.dto.*;
import com.stripe.model.Customer;
import jakarta.validation.Valid;

import java.util.List;

public interface StripeService {

    String createCustomer(@Valid CreateCustomerRequest request);

    Customer retrieveCustomer(String customerId);

    String updateMetaData(String customerId);

    String deleteCustomer(String customerId);

    List<StripeResponse> allCustomers();

    String createProduct(ProductRequest request);

    Response createPaymentIntent(PaymentIntentRequest request);
}