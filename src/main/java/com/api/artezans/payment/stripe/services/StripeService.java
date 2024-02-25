package com.api.artezans.payment.stripe.services;


import com.api.artezans.exceptions.TaskHubException;
import com.api.artezans.payment.stripe.dto.*;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Product;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.ProductCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService {

    @Value("${stripe.api.secretKey}")
    private String stripeApiKeySK;

    @PostConstruct
    private void addStripeSecretKey() {
        Stripe.apiKey = stripeApiKeySK;
    }

    public String createCustomer(@Valid CreateCustomerRequest request) {
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setName(request.getName())
                .setEmail(request.getEmail())
                .setPhone(request.getPhone())
                .setDescription(request.getDescription()) //this will tell who they are, either customer or service provider
                .build();
        try {
            Customer customer = Customer.create(params);
            return customer.getId();
        } catch (StripeException e) {
            throw new TaskHubException(e.getMessage());
        }
    }

    public Customer retrieveCustomer(String customerId) {
        try {
            return Customer.retrieve(customerId);
        } catch (StripeException e) {
            throw new TaskHubException(e.getMessage());
        }
    }

    private LocalDateTime getCreatedAt(Long created) {
        Instant instant = Instant.ofEpochSecond(created);
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public String updateMetaData(String customerId) {
        try {
            Customer customer = Customer.retrieve(customerId);
            CustomerUpdateParams updateParams = CustomerUpdateParams.builder()
                    .putMetadata("order_id", "6735")
                    .build();

            customer.update(updateParams);
        } catch (StripeException e) {
            throw new TaskHubException(e.getMessage());
        }
        return "update successfully";
    }

    public String deleteCustomer(String customerId) {
        try {
            Customer customer = Customer.retrieve(customerId);
            Customer deletedCustomer = customer.delete();
            return deletedCustomer.toJson();
        } catch (StripeException e) {
            throw new TaskHubException(e.getMessage());
        }
    }

    public List<StripeResponse> allCustomers() {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", 3);
        CustomerCollection customerCollection;
        try {
            customerCollection = Customer.list(params);
        } catch (StripeException e) {
            throw new TaskHubException(e.getMessage());
        }
        List<StripeResponse> stripeResponses = new ArrayList<>();
        for (Customer customer : customerCollection.autoPagingIterable()) {
            LocalDateTime createdAt = getCreatedAt(customer.getCreated());
            stripeResponses.add(
                    StripeResponse.builder()
                            .id(customer.getId())
                            .name(customer.getName())
                            .email(customer.getEmail())
                            .phone(customer.getPhone())
                            .balance(BigDecimal.valueOf(customer.getBalance()))
                            .description(customer.getDescription())
                            .created(createdAt)
                            .address(customer.getAddress())
                            .build()
            );
        }
        return stripeResponses;
    }

    public String createProduct(ProductRequest request) {
        ProductCreateParams params = ProductCreateParams.builder()
                .setName(request.getServiceName())
                .setDescription(request.getServiceDescription())
                .setActive(request.isActive())
                .putMetadata("Service Provider ID", request.getServiceProviderStripeId())
//                .setDefaultPriceData(ProductCreateParams.DefaultPriceData.builder()
//                        .setCurrency("AUD")
//                        .setUnitAmount(request.getServicePricePerUnit() * 100)
//                        .build())
                .build();
        Product product;
        try {
            product = Product.create(params);
        } catch (StripeException e) {
            throw new TaskHubException(e.getMessage());
        }
        return product.getId();
    }

    public Response createPaymentIntent(PaymentIntentRequest request) {
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(request.getAmount() * 100L)
                        .putMetadata("Service", request.getServiceName())
                        .setCurrency("aud")
                        .setReceiptEmail(request.getReceiptEmail())
                        .setCustomer(request.getCustomerId())
                        .setDescription("This payment is for " + request.getServiceName() + " service")
                        .putMetadata("Product Owner", request.getProductOwner())
                        .putMetadata("Product ID", request.getProductId())
                        .putMetadata("Booking ID", request.getBookingId())
//                        .setAutomaticPaymentMethods(
//                                PaymentIntentCreateParams
//                                        .AutomaticPaymentMethods
//                                        .builder()
//                                        .setEnabled(true)
//                                        .build()
//                        )
                        .build();
        PaymentIntent intent;
        try {
            intent = PaymentIntent.create(params);
            return Response.builder()
                    .intentID(intent.getId())
                    .clientSecret(intent.getClientSecret())
                    .build();
        } catch (StripeException e) {
            throw new TaskHubException(e.getMessage());
        }
    }
}