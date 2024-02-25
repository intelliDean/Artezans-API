package com.api.artezans.booking.controller;

import com.api.artezans.booking.data.dto.BookingRequest;
import com.api.artezans.booking.data.dto.RejectionRequest;
import com.api.artezans.booking.service.BookingService;
import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.exceptions.TaskHubException;
import com.api.artezans.payment.stripe.dto.Response;
import com.api.artezans.utils.ApiResponse;
import com.paypal.api.payments.Payment;
import com.stripe.exception.StripeException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    public ApiResponse bookService(BookingRequest request, SecuredUser securedUser) {
        return bookingService.bookService(request, securedUser.getUser());
    }

    public ApiResponse acceptProposal(Long bookingId) {
        return bookingService.acceptProposal(bookingId);
    }

    public ApiResponse rejectProposal(Long bookingId) {
        return bookingService.rejectProposal(bookingId);
    }

    public ApiResponse completeTask(Long bookingId) {
        return bookingService.completeTask(bookingId);
    }

    public Response createPaymentIntent(Long bookingId, SecuredUser securedUser) {
        try {
            return bookingService.createPaymentIntentWithStripe(bookingId, securedUser.getUser());
        } catch (StripeException e) {
            throw new TaskHubException(e.getMessage());
        }
    }

    public ApiResponse authorizePaymentWithPaypal(Long bookingId, SecuredUser securedUser) {
        return bookingService.authorizePaymentWithPaypal(bookingId, securedUser.getUser());
    }

    public Payment executePaymentWithPaypal(String paymentId, String payerId) {
        return bookingService.executePaymentWithPaypal(paymentId, payerId);
    }

    public ApiResponse sendServiceProviderNotificationAfterPayment(Long bookingId) {
        return bookingService.sendServiceProviderNotificationAfterPayment(bookingId);
    }

    public ApiResponse customerAcceptService(Long bookingId) {
        return bookingService.customerAcceptService(bookingId);
    }

    public ApiResponse customerRejectService(RejectionRequest request) {
        return bookingService.customerRejectService(request.getBookingId(), request.getRejectionReason());
    }

    public ApiResponse updateBookingAfterPayment(Long bookingId) {
        return bookingService.updateBookingAfterPayment(bookingId);
    }

    public ApiResponse generateInvoice(Long bookingId) {
        return bookingService.generateInvoice(bookingId);
    }


}
