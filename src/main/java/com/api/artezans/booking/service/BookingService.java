package com.api.artezans.booking.service;

import com.api.artezans.booking.data.dto.BookingRequest;
import com.api.artezans.payment.stripe.dto.Response;
import com.api.artezans.users.models.User;
import com.api.artezans.utils.ApiResponse;
import com.paypal.api.payments.Payment;
import com.stripe.exception.StripeException;

public interface BookingService {

    ApiResponse bookService(BookingRequest request, User user);

    ApiResponse acceptProposal(Long bookingId);

    ApiResponse rejectProposal(Long bookingId);

    ApiResponse completeTask(Long bookingId);

    Response createPaymentIntentWithStripe(Long bookingId, User user) throws StripeException;
    ApiResponse authorizePaymentWithPaypal(Long bookingId, User user);
    Payment executePaymentWithPaypal(String paymentId, String payerId);
    ApiResponse sendServiceProviderNotificationAfterPayment(Long bookingId);
    ApiResponse customerAcceptService(Long bookingId);
    ApiResponse customerRejectService(Long bookingId, String reason);
    ApiResponse updateBookingAfterPayment(Long bookingId);

    ApiResponse generateInvoice(Long bookingId);

}
