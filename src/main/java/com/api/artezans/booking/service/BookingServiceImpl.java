package com.api.artezans.booking.service;

import com.api.artezans.booking.data.dto.BookingRequest;
import com.api.artezans.booking.data.dto.InvoiceResponse;
import com.api.artezans.booking.data.dto.RejectionRequest;
import com.api.artezans.booking.data.model.Booking;
import com.api.artezans.booking.data.model.BookingAgreement;
import com.api.artezans.booking.data.model.enums.AgreementStatus;
import com.api.artezans.booking.data.model.enums.BookingStage;
import com.api.artezans.booking.data.model.enums.BookingState;
import com.api.artezans.booking.data.repository.BookingRepository;
import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.exceptions.ArtezanException;
import com.api.artezans.exceptions.UserNotAuthorizedException;
import com.api.artezans.listings.data.models.Listing;
import com.api.artezans.listings.services.ListingService;
import com.api.artezans.notifications.app_notification.model.AppNotification;
import com.api.artezans.notifications.app_notification.service.AppNotificationService;
import com.api.artezans.notifications.mail.MailService;
import com.api.artezans.notifications.dto.EmailRequest;
import com.api.artezans.notifications.dto.MailInfo;
import com.api.artezans.payment.paypal.PaypalService;
import com.api.artezans.payment.paypal.dto.OrderDetail;
import com.api.artezans.payment.stripe.dto.PaymentIntentRequest;
import com.api.artezans.payment.stripe.dto.Response;
import com.api.artezans.payment.stripe.services.StripeServiceImpl;
import com.api.artezans.users.models.User;
import com.api.artezans.utils.ApiResponse;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.api.artezans.booking.data.model.enums.BookingStage.PAID;
import static com.api.artezans.booking.data.model.enums.BookingState.COMPLETED;
import static com.api.artezans.booking.data.model.enums.BookingState.OPEN;
import static com.api.artezans.utils.ApiResponse.apiResponse;
import static com.api.artezans.utils.ArtezanUtils.ACCEPTED;
import static com.api.artezans.utils.ArtezanUtils.REJECTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    @Value("${success.url}")
    private String successUrl;

    @Value("${cancel.url}")
    private String cancelUrl;

    @Value("${charges}")
    private double charges;

    private final AppNotificationService appNotificationService;
    private final MailService mailService;
    private final BookingRepository bookingRepository;
    private final TemplateEngine templateEngine;
    private final ListingService listingService;
    private final StripeServiceImpl stripeServiceImpl;
    private final PaypalService paypalService;

    // NOTE: Context is NOT injected as a shared bean — a new instance is created per
    // email send to prevent race conditions and variable bleed in concurrent requests.

    // ---------------------------------------------------------------------------
    // Booking lifecycle
    // ---------------------------------------------------------------------------

    @Override
    public List<Booking> findBookingsByUser(User user) {
        return bookingRepository.findAllByUser(user);
    }

    @Override
    @Transactional(propagation = REQUIRED)
    public ApiResponse bookService(BookingRequest request, SecuredUser securedUser) {

        LocalTime start = LocalTime.of(request.bookFrom().hour(), request.bookFrom().minute());
        LocalTime end = LocalTime.of(request.bookTo().hour(), request.bookTo().minute());
        if (start.isAfter(end)) {
            throw new ArtezanException("Booking start time cannot be after end time");
        }

        Listing listing = listingService.userFindsListingById(request.listingId());
        if (!listing.isAvailable()) {
            throw new ArtezanException("This listing is currently unavailable for booking");
        }

        Set<LocalDate> dates = validateAndExtractDates(request.bookDates(), listing);
        BigDecimal totalCost = calculateTotalCost(dates, start, end, listing);

        Booking booking = Booking.builder()
                .bookFrom(start)
                .bookTo(end)
                .bookingStage(BookingStage.PROPOSED)
                .bookState(OPEN)
                .bookDates(dates)
                .user(securedUser.getUser())
                .listing(listing)
                .accepted(false)
                .totalCost(totalCost)
                .build();

        bookingRepository.save(booking);
        appNotificationService.saveNotifications(buildNewBookingNotification(listing));
        log.info("Booking proposal [id={}] created for listing [id={}]", booking.getId(), listing.getId());
        return apiResponse(booking, "Booking proposal sent to service provider");
    }

    @Override
    public ApiResponse acceptProposal(Long bookingId, SecuredUser securedUser) {
        Booking booking = getBookingById(bookingId);

        assertIsServiceProviderOfBooking(booking, securedUser,
                "Only the service provider can accept a booking proposal");

        if (!booking.getBookingStage().equals(BookingStage.PROPOSED)) {
            throw new ArtezanException("Booking is not in a proposed state");
        }
        booking.setAccepted(true);
        booking.setBookingStage(BookingStage.ACCEPTED);
        booking.setBookState(OPEN);
        booking.setUpdatedAt(LocalDateTime.now());
        createUserNotification(booking, ACCEPTED);
        bookingRepository.save(booking);
        log.info("Booking [id={}] accepted by provider [email={}]", bookingId, securedUser.getUsername());
        return apiResponse("Booking proposal accepted");
    }

    @Override
    public ApiResponse rejectProposal(Long bookingId, SecuredUser securedUser) {
        Booking booking = getBookingById(bookingId);

        assertIsServiceProviderOfBooking(booking, securedUser,
                "Only the service provider can reject a booking proposal");

        if (!booking.getBookingStage().equals(BookingStage.PROPOSED)) {
            throw new ArtezanException("Booking is not in a proposed state");
        }
        booking.setAccepted(false);
        booking.setBookingStage(BookingStage.REJECTED);
        booking.setBookState(BookingState.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        createUserNotification(booking, REJECTED);
        bookingRepository.save(booking);
        log.info("Booking [id={}] rejected by provider [email={}]", bookingId, securedUser.getUsername());
        return apiResponse("Booking proposal rejected");
    }

    @Override
    @Transactional(propagation = REQUIRED)
    public ApiResponse completeTask(Long bookingId, SecuredUser securedUser) {
        Booking booking = getBookingById(bookingId);

        assertIsServiceProviderOfBooking(booking, securedUser,
                "Only the service provider can mark a task as complete");

        if (!booking.getBookState().equals(OPEN) || !booking.getBookingStage().equals(PAID)) {
            throw new ArtezanException("Task is either not an open booking or has not been paid for");
        }
        booking.setBookState(COMPLETED);
        booking.setUpdatedAt(LocalDateTime.now());
        notifyCustomerOfCompletion(booking);
        bookingRepository.save(booking);
        log.info("Booking [id={}] marked as completed by provider [email={}]", bookingId, securedUser.getUsername());
        return apiResponse("Service provider has delivered the service");
    }

    // ---------------------------------------------------------------------------
    // Payment
    // ---------------------------------------------------------------------------

    @Override
    public Response createPaymentIntentWithStripe(Long bookingId, User user) {
        Booking booking = getBookingById(bookingId);
        if (!booking.getBookingStage().equals(BookingStage.ACCEPTED) || !booking.getUser().equals(user)) {
            throw new ArtezanException("Unauthorized: booking not accepted or invalid user");
        }
        Listing listing = booking.getListing();

        PaymentIntentRequest intentRequest = PaymentIntentRequest.builder()
                .amount(booking.getTotalCost().longValue())
                .bookingId(String.valueOf(bookingId))
                .serviceName(listing.getServiceName())
                .productId(listing.getStripeId())
                .customerId(user.getStripeId())
                .productOwner(listing.getServiceProvider().getUser().getStripeId())
                .receiptEmail(user.getEmailAddress())
                .build();
        return stripeServiceImpl.createPaymentIntent(intentRequest);


    }

    @Override
    public ApiResponse authorizePaymentWithPaypal(Long bookingId, User user) {
        Booking booking = getBookingById(bookingId);
        if (!booking.getBookingStage().equals(BookingStage.ACCEPTED) || !booking.getUser().equals(user)) {
            throw new ArtezanException("Unauthorized: booking not accepted or invalid user");
        }
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setEmail(user.getEmailAddress())
                .setServiceName(booking.getListing().getServiceName())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setTotal(booking.getTotalCost().doubleValue());
        try {
            String approvalUrl = paypalService.authorizePayment(orderDetail, cancelUrl, successUrl);
            return apiResponse(approvalUrl, "Payment authorized successfully");
        } catch (PayPalRESTException e) {
            log.error("PayPal authorization failed for booking [id={}]: {}", bookingId, e.getMessage());
            throw new ArtezanException("PayPal authorization failed: " + e.getMessage());
        }
    }

    @Override
    public Payment executePaymentWithPaypal(String paymentId, String payerId) {
        try {
            return paypalService.executePayment(paymentId, payerId);
        } catch (PayPalRESTException e) {
            log.error("PayPal execution failed [paymentId={}]: {}", paymentId, e.getMessage());
            throw new ArtezanException("PayPal payment execution failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(propagation = REQUIRED)
    public ApiResponse updateBookingAfterPayment(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        if (!booking.getBookState().equals(OPEN) || !booking.getBookingStage().equals(BookingStage.ACCEPTED)) {
            throw new ArtezanException("Booking is not open or the proposal has not been accepted");
        }
        booking.setBookingStage(PAID);
        bookingRepository.save(booking);
        log.info("Booking [id={}] marked as PAID", bookingId);
        return apiResponse("Booking payment status updated");
    }

    @Override
    @Transactional(propagation = REQUIRED)
    public ApiResponse sendServiceProviderNotificationAfterPayment(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        if (!booking.getBookingStage().equals(PAID)) {
            throw new ArtezanException("Cannot notify provider: booking has not been paid for");
        }
        AppNotification notification = AppNotification.builder()
                .notificationTime(LocalDateTime.now())
                .recipient(booking.getListing().getServiceProvider().getUser())
                .message(String.format("A customer has paid for your service: %s", booking.getListing().getServiceName()))
                .build();
        appNotificationService.saveNotifications(notification);
        sendPaymentConfirmationToServiceProvider(booking);
        return apiResponse("Notification sent successfully");
    }

    // ---------------------------------------------------------------------------
    // Service agreement (customer accept / reject)
    // ---------------------------------------------------------------------------

    @Override
    @Transactional(propagation = REQUIRED)
    public ApiResponse customerAcceptService(Long bookingId, SecuredUser currentUser) {
        Booking booking = getBookingById(bookingId);

        assertIsCustomerOfBooking(booking, currentUser.getUser(),
                "Only the customer who placed this booking can accept the service");

        if (booking.getBookingAgreement() != null
                && booking.getBookingAgreement().getAgreementStatus().equals(AgreementStatus.ACCEPTED)) {
            throw new ArtezanException("Service already accepted and deal closed");
        }

        // Accept (or re-accept after a prior REJECTED agreement)
        applyAgreement(booking, AgreementStatus.ACCEPTED, "Service is accepted");
        booking.setBookState(COMPLETED);
        bookingRepository.save(booking);
        sendMailToServiceProvider(booking, "accept_service", " Service Accepted");
        log.info("Booking [id={}] accepted by customer [email={}]", bookingId, currentUser.getUsername());
        return apiResponse("Service accepted successfully");
    }

    @Override
    @Transactional(propagation = REQUIRED)
    public ApiResponse customerRejectService(RejectionRequest request, SecuredUser currentUser) {
        Booking booking = getBookingById(request.bookingId());

        assertIsCustomerOfBooking(
                booking,
                currentUser.getUser(),
                "Only the customer who placed this booking can reject the service"
        );

        if (!booking.getBookingStage().equals(PAID)) {
            throw new ArtezanException("Service has not been paid for yet");
        }

        BookingAgreement agreement = booking.getBookingAgreement();
        if (agreement != null) {
            if (agreement.getAgreementStatus().equals(AgreementStatus.REJECTED)) {
                throw new ArtezanException("Service has already been rejected");
            }
            if (agreement.getAgreementStatus().equals(AgreementStatus.ACCEPTED)) {
                throw new ArtezanException("Service has already been accepted and the deal is closed");
            }
        }

        applyAgreement(booking, AgreementStatus.REJECTED, request.rejectionReason());
        bookingRepository.save(booking);
        sendMailToServiceProvider(booking, "reject_service", " Service Rejected");
        log.info("Booking [id={}] rejected by customer. Reason: {}", request.bookingId(), request.rejectionReason());
        return apiResponse("Service rejected successfully");
    }

    // ---------------------------------------------------------------------------
    // Invoice
    // ---------------------------------------------------------------------------

    @Override
    public ApiResponse generateInvoice(Long bookingId) {
        Booking booking = getBookingById(bookingId);

        BookingAgreement agreement = booking.getBookingAgreement();
        if (agreement == null || !agreement.getAgreementStatus().equals(AgreementStatus.ACCEPTED)) {
            throw new ArtezanException("Error generating invoice: service has not been accepted by the customer");
        }
        if (!booking.getBookingStage().equals(PAID)) {
            throw new ArtezanException("Error generating invoice: booking has not been paid for");
        }

        User serviceProviderUser = booking.getListing().getServiceProvider().getUser();
        String customerName = booking.getUser().getFirstName() + " " + booking.getUser().getLastName();
        int hoursWorked = calculateHoursWorked(booking);
        BigDecimal totalAmount = calculateTotalAfterCharges(booking);

        InvoiceResponse invoiceResponse = InvoiceResponse.builder()
                .serviceProvider(serviceProviderUser.getFirstName() + " " + serviceProviderUser.getLastName())
                .serviceName(booking.getListing().getServiceName())
                .businessName(booking.getListing().getBusinessName())
                .serviceCategory(booking.getListing().getServiceCategory())
                .customerName(customerName)
                .numberOfDaysWorked(booking.getBookDates().size())
                .numberOfHoursWorked(hoursWorked)
                .subTotal(booking.getTotalCost())
                .total(totalAmount)
                .pricePerUnit(booking.getListing().getPricing())
                .bookedAt(booking.getBookedAt())
                .build();

        return apiResponse(invoiceResponse, "Invoice generated successfully");
    }

    // ---------------------------------------------------------------------------
    // Private helpers — notifications & email
    // ---------------------------------------------------------------------------

    private void notifyCustomerOfCompletion(Booking booking) {
        User customer = booking.getUser();
        String providerName = booking.getListing().getServiceProvider().getUser().getFirstName();
        String serviceName = booking.getListing().getServiceName();

        AppNotification notification = AppNotification.builder()
                .notificationTime(LocalDateTime.now())
                .recipient(customer)
                .message(String.format(
                        "Dear %s, %s has completed the %s service assigned to them. Please inspect and respond.",
                        customer.getFirstName(), providerName, serviceName))
                .build();
        appNotificationService.saveNotifications(notification);

        Context ctx = new Context();
        ctx.setVariables(Map.of(
                "firstName", customer.getFirstName(),
                "serviceProviderName", providerName,
                "serviceName", serviceName
        ));
        String content = templateEngine.process("complete_task", ctx);
        EmailRequest emailRequest = EmailRequest.builder()
                .subject("Completion of " + serviceName + " service")
                .to(List.of(new MailInfo(customer.getFirstName(), customer.getEmailAddress())))
                .htmlContent(content)
                .build();
        mailService.sendMail(emailRequest);
    }

    private void sendMailToServiceProvider(Booking booking, String templateName, String subjectSuffix) {
        String serviceName = booking.getListing().getServiceName();
        User providerUser = booking.getListing().getServiceProvider().getUser();
        String customer = booking.getUser().getFirstName();

        Context ctx = new Context();
        ctx.setVariables(Map.of(
                "firstName", providerUser.getFirstName(),
                "customerName", customer,
                "serviceName", serviceName
        ));
        String content = templateEngine.process(templateName, ctx);
        EmailRequest emailRequest = EmailRequest.builder()
                .subject(serviceName + subjectSuffix)
                .to(List.of(new MailInfo(providerUser.getFirstName(), providerUser.getEmailAddress())))
                .htmlContent(content)
                .build();
        mailService.sendMail(emailRequest);
    }

    private void sendPaymentConfirmationToServiceProvider(Booking booking) {
        User providerUser = booking.getListing().getServiceProvider().getUser();
        String customer = booking.getUser().getFirstName();
        // Use sorted order for deterministic date display — Set has no guaranteed iteration order
        LocalDate firstDate = booking.getBookDates().stream().sorted().findFirst()
                .orElseThrow(() -> new ArtezanException("Booking has no dates"));

        Context ctx = new Context();
        ctx.setVariables(Map.of(
                "firstName", providerUser.getFirstName(),
                "customer", customer,
                "amount", booking.getTotalCost().toString(),
                "serviceName", booking.getListing().getServiceName(),
                "date", firstDate.toString(),
                "time", booking.getBookFrom().toString()
        ));
        String content = templateEngine.process("payment_confirmation", ctx);
        EmailRequest request = EmailRequest.builder()
                .subject("Payment Notification")
                .to(List.of(new MailInfo(providerUser.getFirstName(), providerUser.getEmailAddress())))
                .htmlContent(content)
                .build();
        mailService.sendMail(request);
    }

    private void createUserNotification(Booking booking, String message) {
        AppNotification notification = AppNotification.builder()
                .message(message)
                .recipient(booking.getUser())
                .notificationTime(LocalDateTime.now())
                .build();
        appNotificationService.saveNotifications(notification);
    }

    private static AppNotification buildNewBookingNotification(Listing listing) {
        return AppNotification.builder()
                .notificationTime(LocalDateTime.now())
                .recipient(listing.getServiceProvider().getUser())
                .message("You have a new " + listing.getServiceName() + " booking proposal")
                .build();
    }

    // ---------------------------------------------------------------------------
    // Private helpers — business logic
    // ---------------------------------------------------------------------------

    /**
     * Applies or updates a {@link BookingAgreement} on the given booking.
     * Creates a new agreement if one does not yet exist.
     */
    private void applyAgreement(Booking booking, AgreementStatus status, String message) {
        BookingAgreement agreement = booking.getBookingAgreement();
        if (agreement == null) {
            agreement = new BookingAgreement();
            booking.setBookingAgreement(agreement);
        }
        agreement.setAgreementStatus(status)
                .setMessage(message)
                .setAgreementTime(LocalDateTime.now());
    }

    /**
     * Validates that each requested booking date falls on a day the service provider is available,
     * then returns the validated set.
     */
    private Set<LocalDate> validateAndExtractDates(Set<LocalDate> requestedDates, Listing listing) {
        // Convert available days to a Set<String> of lowercase day names for O(1) lookup
        Set<String> availableDayNames = listing.getAvailableDays().stream()
                .map(day -> day.name().toLowerCase())
                .collect(Collectors.toSet());

        Set<LocalDate> validated = new HashSet<>();
        for (LocalDate date : requestedDates) {
            String dayOfWeek = date.getDayOfWeek().toString().toLowerCase();
            if (!availableDayNames.contains(dayOfWeek)) {
                throw new ArtezanException(
                        "Service provider is unavailable on " + date.getDayOfWeek().toString() + "s");
            }
            validated.add(date);
        }
        return validated;
    }

    /**
     * Calculates the total cost: price × number of days × hours per day.
     */
    private BigDecimal calculateTotalCost(Set<LocalDate> dates, LocalTime start, LocalTime end, Listing listing) {
        BigDecimal days = BigDecimal.valueOf(dates.size());
        BigDecimal hours = BigDecimal.valueOf(Duration.between(start, end).toHours());
        return listing.getPricing().multiply(days).multiply(hours);
    }

    /**
     * Returns the service fee retained by Artezan, deducted from the total booking cost.
     */
    private BigDecimal calculateTotalAfterCharges(Booking booking) {
        return booking.getTotalCost().subtract(
                booking.getTotalCost().multiply(BigDecimal.valueOf(charges))
        );
    }

    /**
     * Returns the total hours worked on a booking (difference between bookFrom and bookTo).
     */
    private int calculateHoursWorked(Booking booking) {
        return (int) Duration.between(booking.getBookFrom(), booking.getBookTo()).toHours();
    }

    /**
     * Looks up a booking by ID or throws {@link ArtezanException} with a clear message.
     */
    private Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ArtezanException("Booking could not be found"));
    }

    /**
     * Asserts that the caller is the service provider who owns the listing linked to this booking.
     * Throws {@link com.api.artezans.exceptions.UserNotAuthorizedException} if not.
     */
    private void assertIsServiceProviderOfBooking(Booking booking, SecuredUser securedUser, String message) {
        if (!booking.getListing().getServiceProvider().getUser().equals(securedUser.getUser())) {
            throw new UserNotAuthorizedException(message);
        }
    }

    /**
     * Asserts that the caller is the customer who placed this booking.
     * Throws {@link com.api.artezans.exceptions.UserNotAuthorizedException} if not.
     */
    private void assertIsCustomerOfBooking(Booking booking, User user, String message) {
        if (!booking.getUser().equals(user)) {
            log.warn("Unauthorized booking action attempted by [email={}] on booking [id={}]",
                    user.getEmailAddress(), booking.getId());
            throw new UserNotAuthorizedException(message);
        }
    }
}