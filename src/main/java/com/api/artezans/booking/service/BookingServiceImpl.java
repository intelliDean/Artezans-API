package com.api.artezans.booking.service;

import com.api.artezans.booking.data.dto.BookingRequest;
import com.api.artezans.booking.data.dto.InvoiceResponse;
import com.api.artezans.booking.data.model.Booking;
import com.api.artezans.booking.data.model.BookingAgreement;
import com.api.artezans.booking.data.model.enums.AgreementStatus;
import com.api.artezans.booking.data.model.enums.BookingStage;
import com.api.artezans.booking.data.model.enums.BookingState;
import com.api.artezans.booking.data.repository.BookingRepository;
import com.api.artezans.exceptions.TaskHubException;
import com.api.artezans.listings.data.enums.AvailableDays;
import com.api.artezans.listings.data.models.Listing;
import com.api.artezans.listings.services.ListingService;
import com.api.artezans.notifications.app_notification.model.AppNotification;
import com.api.artezans.notifications.app_notification.service.AppNotificationService;
import com.api.artezans.notifications.mail.MailService;
import com.api.artezans.notifications.mail.dto.EmailRequest;
import com.api.artezans.notifications.mail.dto.MailInfo;
import com.api.artezans.payment.paypal.PaypalService;
import com.api.artezans.payment.paypal.dto.OrderDetail;
import com.api.artezans.payment.stripe.dto.PaymentIntentRequest;
import com.api.artezans.payment.stripe.services.StripeService;
import com.api.artezans.users.models.User;
import com.api.artezans.utils.ApiResponse;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
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

import static com.api.artezans.booking.data.model.enums.BookingStage.PAID;
import static com.api.artezans.booking.data.model.enums.BookingState.COMPLETED;
import static com.api.artezans.booking.data.model.enums.BookingState.OPEN;
import static com.api.artezans.utils.ApiResponse.apiResponse;
import static com.api.artezans.utils.TaskHubUtils.ACCEPTED;
import static com.api.artezans.utils.TaskHubUtils.REJECTED;
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
    private final StripeService stripeService;
    private final PaypalService paypalService;
    private final Context context;

    @Override
    public ApiResponse bookService(BookingRequest request, User user) {
        LocalTime start = LocalTime.of(request.getBookFrom().getHour(), request.getBookFrom().getMinute());
        LocalTime end = LocalTime.of(request.getBookTo().getHour(), request.getBookTo().getMinute());
        if (start.isAfter(end)) throw new TaskHubException("Time is incoherent");

        Listing listing = listingService.userFindsListingById(request.getListingId());
        Set<LocalDate> dates = getDates(request.getBookDates(), listing);
        BigDecimal totalCost = getTotalCost(dates, start, end, listing);
        Booking booking = Booking.builder()
                .bookFrom(start)
                .bookTo(end)
                .bookingStage(BookingStage.PROPOSED)
                .bookState(OPEN)
                .bookDates(dates)
                .user(user)
                .listing(listing)
                .accepted(false)
                .totalCost(totalCost)
                .build();
        bookingRepository.save(booking);
        appNotificationService.saveNotifications(getAppNotification(listing));
        return apiResponse("Booking Proposal sent to Service");
    }

    private static AppNotification getAppNotification(Listing listing) {
        return AppNotification.builder()
                .notificationTime(LocalDateTime.now())
                .recipient(listing.getServiceProvider().getUser())
                .message("You have a new " + listing.getServiceName() + " booking")
                .build();
    }

    @Override
    public ApiResponse acceptProposal(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        if (booking.getBookingStage().equals(BookingStage.PROPOSED)) {
            booking.setAccepted(true);
            booking.setBookingStage(BookingStage.ACCEPTED);
            booking.setBookState(OPEN);
            booking.setUpdatedAt(LocalDateTime.now());
            createNotification(booking, ACCEPTED);
            bookingRepository.save(booking);
            return apiResponse("Booking Proposal accepted");
        }
        throw new TaskHubException("Not a proposal");
    }

    private Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new TaskHubException("Booking could not be found"));
    }

    @Override
    public ApiResponse rejectProposal(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        if (booking.getBookingStage().equals(BookingStage.PROPOSED)) {
            booking.setAccepted(false);
            booking.setBookingStage(BookingStage.REJECTED);
            booking.setBookState(BookingState.CANCELLED);
            booking.setUpdatedAt(LocalDateTime.now());
            createNotification(booking, REJECTED);
            bookingRepository.save(booking);
            return apiResponse("Booking proposal rejected");
        }
        throw new TaskHubException("Booking proposal rejected");
    }

    @Override
    @Transactional(propagation = REQUIRED)
    public ApiResponse completeTask(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        if (booking.getBookState().equals(OPEN) &&
                booking.getBookingStage().equals(PAID)) {
            booking.setBookState(COMPLETED);
            booking.setUpdatedAt(LocalDateTime.now());
            notifyCustomer(booking);
            bookingRepository.save(booking);
            return apiResponse("Service provider delivered service");
        }
//        booking.setBookingStage(PAID);
        throw new TaskHubException("Task is either not an open booking or the booking is not yet paid for");
    }

    private void notifyCustomer(Booking booking) {
        User user = booking.getUser();
        String serviceProvider = booking.getListing().getServiceProvider().getUser().getFirstName();
        String serviceName = booking.getListing().getServiceName();
        AppNotification appNotification = AppNotification.builder()
                .notificationTime(LocalDateTime.now())
                .recipient(user)
                .message(String.format("""
                        Dear %s,
                        %s has completed the %s service assigned to him.
                        Inspect and revert.
                        """, user.getFirstName(), serviceProvider, serviceName))
                .build();
        appNotificationService.saveNotifications(appNotification);
        sendNotificationMailToCustomer(user, serviceProvider, serviceName);
    }

    private void sendNotificationMailToCustomer(User user, String serviceProvider, String serviceName) {
        context.setVariables(Map.of(
                "firstName", user.getFirstName(),
                "serviceProviderName", serviceProvider,
                "serviceName", serviceName
        ));
        String content = templateEngine.process("complete_task", context);
        EmailRequest emailRequest = EmailRequest.builder()
                .subject("Completion of " + serviceName + " service")
                .to(Collections.singletonList(new MailInfo(user.getFirstName(), user.getEmailAddress())))
                .htmlContent(content)
                .build();
        mailService.sendMail(emailRequest);
    }

    @Override
    public Response createPaymentIntentWithStripe(Long bookingId, User user) {
        Booking booking = getBookingById(bookingId);
        if (booking.getBookingStage().equals(BookingStage.ACCEPTED)
                && booking.getUser().equals(user)) {
            Listing listing = booking.getListing();
            PaymentIntentRequest intentRequest = PaymentIntentRequest.builder()
                    .amount(booking.getTotalCost().longValue())
                    .serviceName(listing.getServiceName())
                    .productId(listing.getStripeId())
                    .customerId(user.getStripeId())
                    .productOwner(listing.getServiceProvider().getUser().getStripeId())
                    .receiptEmail(user.getEmailAddress())
                    .build();
            return stripeService.createPaymentIntent(intentRequest);
        }
        throw new TaskHubException("Unauthorized: Booking not accepted or invalid user");
    }

    @Override
    public ApiResponse authorizePaymentWithPaypal(Long bookingId, User user) {
        Booking booking = getBookingById(bookingId);
        if (booking.getBookingStage().equals(BookingStage.ACCEPTED) && booking.getUser().equals(user)) {
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
                throw new TaskHubException(e.getMessage());
            }
        }
        throw new TaskHubException("Unauthorized: Booking not accepted or invalid user");
    }

    @Override
    public Payment executePaymentWithPaypal(String paymentId, String payerId) {
        try {
            return paypalService.executePayment(paymentId, payerId);
        } catch (PayPalRESTException e) {
            throw new TaskHubException(e.getMessage());
        }
    }

    @Override
    @Transactional(propagation = REQUIRED)
    public ApiResponse sendServiceProviderNotificationAfterPayment(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        AppNotification appNotification = AppNotification.builder()
                .notificationTime(LocalDateTime.now())
                .recipient(booking.getListing().getServiceProvider().getUser())
                .message(String.format("A customer has paid for your service: %s", booking.getListing().getServiceName()))
                .build();
        sendPaymentConfirmationToServiceProvider(booking);
        appNotificationService.saveNotifications(appNotification);
        return apiResponse("Notification sent successfully");
    }

    @Override
    @Transactional(propagation = REQUIRED)
    public ApiResponse customerAcceptService(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        if (booking.getBookingAgreement() == null) {
            BookingAgreement bookingAgreement = new BookingAgreement();
            bookingAgreement.setAgreementStatus(AgreementStatus.ACCEPTED)
                    .setMessage("Service is accepted")
                    .setAgreementTime(LocalDateTime.now());
            booking.setBookingAgreement(bookingAgreement);
            booking.setBookState(COMPLETED);
            bookingRepository.save(booking);
            sendMailToServiceProvider(booking, "accept_service", " Service Accepted");
            return apiResponse("Accept service successfully");
        } else {
            if (booking.getBookingAgreement().getAgreementStatus().equals(AgreementStatus.ACCEPTED))
                throw new TaskHubException("Service already accepted and deal closed");
            else {
                booking.getBookingAgreement().setAgreementStatus(AgreementStatus.ACCEPTED)
                        .setMessage("Service is accepted")
                        .setAgreementTime(LocalDateTime.now());
                booking.setBookState(COMPLETED);
                bookingRepository.save(booking);
                sendMailToServiceProvider(booking, "accept_service", " Service Accepted");
                return apiResponse("Accept service successfully");
            }
        }
    }

    @Override
    @Transactional(propagation = REQUIRED)
    public ApiResponse customerRejectService(Long bookingId, String reason) {
        Booking booking = getBookingById(bookingId);
        if (booking.getBookingAgreement() == null) {
            if (booking.getBookingStage().equals(PAID)) {
                BookingAgreement bookingAgreement = new BookingAgreement();
                bookingAgreement.setAgreementStatus(AgreementStatus.REJECTED)
                        .setMessage(reason)
                        .setAgreementTime(LocalDateTime.now());
                booking.setBookingAgreement(bookingAgreement);
                bookingRepository.save(booking);
                sendMailToServiceProvider(booking, "reject_service", " Service Rejected");
                return apiResponse("Reject service successfully");
            }
            throw new TaskHubException("Service not yet paid for");
        } else {
            if (booking.getBookingAgreement().getAgreementStatus().equals(AgreementStatus.REJECTED))
                throw new TaskHubException("Service already rejected");
            else if (booking.getBookingAgreement().getAgreementStatus().equals(AgreementStatus.ACCEPTED))
                throw new TaskHubException("Service already accepted and deal closed");
            else {
                if (booking.getBookingStage().equals(PAID)) {
                    booking.getBookingAgreement().setAgreementStatus(AgreementStatus.REJECTED)
                            .setMessage(reason)
                            .setAgreementTime(LocalDateTime.now());
                    bookingRepository.save(booking);
                    sendMailToServiceProvider(booking, "reject_service", " Service Rejected");
                    return apiResponse("Reject service successfully");
                }
                throw new TaskHubException("Service not yet paid for");
            }
        }
    }

    @Override
    public ApiResponse updateBookingAfterPayment(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        if (booking.getBookState().equals(OPEN) && booking.getBookingStage().equals(BookingStage.ACCEPTED)) {
            booking.setBookingStage(PAID);
            bookingRepository.save(booking);
            return apiResponse("Booking payment status updated");
        } else {
            throw new TaskHubException("Booking not opened or booking proposal not accepted");
        }
    }

    @Override
    public ApiResponse generateInvoice(Long bookingId) {
        Booking foundBooking = getBookingById(bookingId);
        if (foundBooking.getBookingAgreement().getAgreementStatus().equals(AgreementStatus.ACCEPTED)
                && foundBooking.getBookingStage().equals(PAID)) {
            String customerName = foundBooking.getUser().getFirstName() + " " + foundBooking.getUser().getLastName();
            int hours = calculateHoursWorked(foundBooking);
            BigDecimal totalAmount = calculateTotalAmount(foundBooking);
            User serviceProvider = foundBooking.getListing().getServiceProvider().getUser();
            InvoiceResponse invoiceResponse = InvoiceResponse.builder()
                    .serviceProvider(serviceProvider.getFirstName() + " " + serviceProvider.getLastName())
                    .serviceName(foundBooking.getListing().getServiceName())
                    .businessName(foundBooking.getListing().getBusinessName())
                    .serviceCategory(foundBooking.getListing().getServiceCategory())
                    .customerName(customerName)
                    .numberOfDaysWorked(foundBooking.getBookDates().size())
                    .numberOfHoursWorked(hours)
                    .subTotal(foundBooking.getTotalCost())
                    .total(totalAmount)
                    .pricePerUnit(foundBooking.getListing().getPricing())
                    .bookedAt(foundBooking.getBookedAt())
                    .build();
            return apiResponse(invoiceResponse, "Invoice generated successfully");
        }
        throw new TaskHubException("Error generating invoice: Service not accepted");
    }

    private BigDecimal calculateTotalAmount(Booking foundBooking) {
        return foundBooking.getTotalCost().subtract(
                foundBooking.getTotalCost().multiply(
                        BigDecimal.valueOf(charges)
                )
        );
    }

    private int calculateHoursWorked(Booking foundBooking) {
        return (int) Duration.between(foundBooking.getBookFrom(), foundBooking.getBookTo())
                .toHours();
    }

    private void sendMailToServiceProvider(Booking booking, String fileName, String subject) {
        String serviceName = booking.getListing().getServiceName();
        String serviceProvider = booking.getListing().getServiceProvider().getUser().getFirstName();
        String emailAddress = booking.getListing().getServiceProvider().getUser().getEmailAddress();
        String customer = booking.getUser().getFirstName();
        context.setVariables(Map.of(
                "firstName", serviceProvider,
                "customerName", customer,
                "serviceName", serviceName
        ));
        String content = templateEngine.process(fileName, context);
        EmailRequest emailRequest = EmailRequest.builder()
                .subject(serviceName + subject)
                .to(Collections.singletonList(new MailInfo(serviceProvider, emailAddress)))
                .htmlContent(content)
                .build();
        mailService.sendMail(emailRequest);
    }

    private void sendPaymentConfirmationToServiceProvider(Booking booking) {
        if (booking.getBookingStage().equals(PAID)) {
            User serviceproviderUser = booking.getListing().getServiceProvider().getUser();
            String serviceProviderName = serviceproviderUser.getFirstName();
            String customer = booking.getUser().getFirstName();
            List<LocalDate> dates = booking.getBookDates().stream().toList();
            context.setVariables(Map.of(
                    "firstName", serviceProviderName,
                    "customer", customer,
                    "amount", booking.getTotalCost().toString(),
                    "service", booking.getListing().getServiceName(),
                    "date", dates.get(0).toString(),
                    "time", booking.getBookFrom().toString()
            ));
            String content = templateEngine.process("payment_confirmation", context);
            EmailRequest request = EmailRequest.builder()
                    .subject("Payment Notification")
                    .to(Collections.singletonList(new MailInfo(
                            serviceProviderName,
                            serviceproviderUser.getEmailAddress()
                    )))
                    .htmlContent(content)
                    .build();
            mailService.sendMail(request);
        } else {
            throw new TaskHubException("Unpaid Service");
        }
    }

    private void createNotification(Booking booking, String message) {
        User user = booking.getUser();
        AppNotification notification = AppNotification.builder()
                .message(message)
                .recipient(user)
                .notificationTime(LocalDateTime.now())
                .build();
        appNotificationService.saveNotifications(notification);
    }

    private BigDecimal getTotalCost(
            Set<LocalDate> dates, LocalTime start, LocalTime end, Listing listing) {

        BigDecimal numberOfDays = BigDecimal.valueOf(dates.size());
        long numberOfHours = Duration.between(start, end).toHours();
        BigDecimal numberOfHoursPerDay = BigDecimal.valueOf(numberOfHours);
        return listing.getPricing().multiply(numberOfDays).multiply(numberOfHoursPerDay);
    }

    private Set<LocalDate> getDates(Set<LocalDate> bookingDates, Listing listing) {
        List<String> days = new ArrayList<>();
        for (AvailableDays day : listing.getAvailableDays()) {
            days.add(day.name().toLowerCase());
        }
        Set<LocalDate> localDates = new HashSet<>();
        for (LocalDate date : bookingDates) {
            String dayOfTheWeek = date.getDayOfWeek().toString().toLowerCase();
            if (!days.contains(dayOfTheWeek)) {
                throw new TaskHubException("Service Provider is unavailable on " + dayOfTheWeek.toUpperCase() + "s");
            }
            localDates.add(date);
        }
        return localDates;
    }
}