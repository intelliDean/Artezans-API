package com.api.artezans.booking.service;

import com.api.artezans.booking.data.dto.BookingRequest;
import com.api.artezans.booking.data.dto.RejectionRequest;
import com.api.artezans.booking.data.model.Booking;
import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.payment.stripe.dto.Response;
import com.api.artezans.users.models.User;
import com.api.artezans.utils.ApiResponse;
import com.paypal.api.payments.Payment;
import com.stripe.exception.StripeException;

import java.util.List;

public interface BookingService {

    List<Booking> findBookingsByUser(User user);

    List<Booking> findBookingsByProvider(User user);

    ApiResponse bookService(BookingRequest request, SecuredUser user);

    ApiResponse acceptProposal(Long bookingId, SecuredUser securedUser);

    ApiResponse rejectProposal(Long bookingId, SecuredUser securedUser);

    ApiResponse completeTask(Long bookingId, SecuredUser securedUser);

    Response createPaymentIntentWithStripe(Long bookingId, User user);

    ApiResponse authorizePaymentWithPaypal(Long bookingId, User user);

    Payment executePaymentWithPaypal(String paymentId, String payerId);

    ApiResponse sendServiceProviderNotificationAfterPayment(Long bookingId);

    ApiResponse customerAcceptService(Long bookingId, SecuredUser securedUser);

    ApiResponse customerRejectService(RejectionRequest resuest, SecuredUser securedUser);

    ApiResponse updateBookingAfterPayment(Long bookingId);

    ApiResponse generateInvoice(Long bookingId);

}
