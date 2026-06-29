package com.api.artezans.gateway.booking;

import com.api.artezans.booking.data.dto.BookingRequest;
import com.api.artezans.booking.data.dto.RejectionRequest;
import com.api.artezans.booking.data.model.Booking;
import com.api.artezans.booking.service.BookingService;
import com.api.artezans.config.annotation.CurrentUser;
import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.payment.stripe.dto.Response;
import com.api.artezans.utils.ApiResponse;
import com.paypal.api.payments.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.api.artezans.gateway.booking.BookingUtil.*;


@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "Booking Controller")
@RequestMapping("api/v1/booking")
public class BookingGateway {

    private final BookingService bookingService;


    @PostMapping("book")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'SERVICE_PROVIDER')")
    @Operation(summary = BOOK_SUMMARY, description = BOOK_DESCRIPTION, operationId = BOOK_OP_ID)
    public ResponseEntity<ApiResponse> bookService(
            @RequestBody BookingRequest request, @CurrentUser SecuredUser securedUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.bookService(request, securedUser));
    }

    @GetMapping("my-bookings")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'SERVICE_PROVIDER')")
    @Operation(summary = "Get bookings of current user")
    public ResponseEntity<List<Booking>> findMyBookings(@CurrentUser SecuredUser currentUser) {
        return ResponseEntity.ok(bookingService.findBookingsByUser(currentUser.getUser()));
    }

    @PostMapping("accept-proposal")
    @Operation(summary = ACCEPT_PROPOSAL_SUM, description = ACCEPT_PROPOSAL_DESC, operationId = ACCEPT_PROPOSAL_OP_ID)
    public ResponseEntity<ApiResponse> acceptProposal(@RequestParam Long bookingId, @CurrentUser SecuredUser currentUser) {
        return ResponseEntity.ok(
                bookingService.acceptProposal(bookingId, currentUser)
        );
    }

    @PostMapping("reject-proposal")
    @Operation(summary = REJECT_PROPOSAL_SUM, description = REJECT_PROPOSAL_DESC, operationId = REJECT_PROPOSAL_OP_ID)
    public ResponseEntity<ApiResponse> rejectProposal(@RequestParam Long bookingId, @CurrentUser SecuredUser currentUser) {
        return ResponseEntity.ok(
                bookingService.rejectProposal(bookingId, currentUser)
        );
    }

    @PostMapping("complete-task")
    @Operation(summary = COMPLETE_SUMMARY, description = COMPLETE_DESCRIPTION, operationId = COMPLETE_OP_ID)
    public ResponseEntity<ApiResponse> dealClosed(@RequestParam Long bookingId, @CurrentUser SecuredUser currentUser) {
        return ResponseEntity.ok(
                bookingService.completeTask(bookingId, currentUser)
        );
    }

    @PostMapping("payment-intent-stripe/{bookingId}")
    @Operation(summary = INTENT_SUMMARY, description = INTENT_DESCRIPTION, operationId = INTENT_OP_ID)
    public ResponseEntity<Response> createPaymentIntentWithStripe(
            @PathVariable Long bookingId, @CurrentUser SecuredUser securedUser) {
        return ResponseEntity.ok(
                bookingService.createPaymentIntentWithStripe(bookingId, securedUser.getUser())
        );
    }

    @PostMapping("authorize-paypal")
    @Operation(summary = AUTHORIZE_SUMMARY, description = AUTHORIZE_DESCRIPTION, operationId = AUTHORIZE_OP_ID)
    public ResponseEntity<ApiResponse> authorizePaymentWithPaypal(@RequestParam Long bookingId, @CurrentUser SecuredUser securedUser) {
        return ResponseEntity.ok(bookingService.authorizePaymentWithPaypal(bookingId, securedUser.getUser()));
    }

    @PostMapping("execute-payment-paypal")
    @Operation(summary = EXECUTE_SUMMARY, description = EXECUTE_DESCRIPTION, operationId = EXECUTE_OP_ID)
    public ResponseEntity<Payment> executePaymentWithPaypal(
            @RequestParam String paymentId, @RequestParam String payerId) {
        return ResponseEntity.ok(bookingService.executePaymentWithPaypal(paymentId, payerId));
    }

    @PostMapping("send-notification")
    @Operation(summary = NOTIFY_SUMMARY, description = NOTIFY_DESCRIPTION, operationId = NOTIFY_OP_ID)
    public ResponseEntity<ApiResponse> sendServiceProviderNotificationAfterPayment(@RequestParam Long bookingId) {
        return ResponseEntity.ok(bookingService.sendServiceProviderNotificationAfterPayment(bookingId));
    }

    @PostMapping("accept-service")
    @Operation(summary = ACCEPT_SERVICE_SUM, description = ACCEPT_SERVICE_DESC, operationId = ACCEPT_SERVICE_OP_ID)
    public ResponseEntity<ApiResponse> customerAcceptService(@RequestParam Long bookingId, @CurrentUser SecuredUser currentUser) {
        return ResponseEntity.ok(bookingService.customerAcceptService(bookingId, currentUser));
    }

    @PostMapping("reject-service")
    @Operation(summary = REJECT_SERVICE_SUM, description = REJECT_SERVICE_DESC, operationId = REJECT_SERVICE_OP_ID)
    public ResponseEntity<ApiResponse> customerRejectService(@RequestBody RejectionRequest request, @CurrentUser SecuredUser currentUser) {
        return ResponseEntity.ok(bookingService.customerRejectService(request, currentUser));
    }

    @PostMapping("update_payment")
    @Operation(summary = UPDATE_PAYMENT_SUM, description = UPDATE_PAYMENT_DESC, operationId = UPDATE_PAYMENT_OP_ID)
    public ResponseEntity<ApiResponse> updateBookingAfterPayment(@RequestParam Long bookingId) {
        return ResponseEntity.ok(
                bookingService.updateBookingAfterPayment(bookingId)
        );
    }

    @PostMapping("generate-invoice")
    @Operation(summary = GENERATE_INVOICE_SUM, description = GENERATE_INVOICE_SUM, operationId = GENERATE_INVOICE_OP_ID)
    public ResponseEntity<ApiResponse> generateInvoice(@RequestParam Long bookingId) {
        return ResponseEntity.ok(
                bookingService.generateInvoice(bookingId)
        );
    }
}
