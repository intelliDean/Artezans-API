package com.api.artezans.payment;

import com.api.artezans.booking.service.BookingService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Charge;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class StripeWebhook {

    private final BookingService bookingService;


    public void handlingEvents(Event event) {

        switch (event.getType()) {

            case "charge.failed" -> {
                log.info("<<>> Charge failed <<>>");
            }
            case "charge.succeeded" -> {
                log.info("<<>> Charge succeeded <<>>");
                event.getDataObjectDeserializer().getObject().ifPresent(stripeObject -> {
                    if (stripeObject instanceof Charge charge) {
                        String bookingIdStr = charge.getMetadata().get("Booking ID");
                        if (bookingIdStr != null) {
                            try {
                                Long bookingId = Long.parseLong(bookingIdStr);
                                bookingService.updateBookingAfterPayment(bookingId);
                            } catch (Exception e) {
                                log.error("Failed to update booking status after Charge succeeded: {}", e.getMessage(), e);
                            }
                        }
                    }
                });
            }
            case "checkout.session.completed" -> {
                log.info("<<>> Checkout Session Completed <<>>");
            }
            case "customer.created" -> {
                log.info("<<>> Customer Created successfully <<>>");
            }
            case "customer.deleted" -> {
                log.info("<<>> Customer Deleted successfully <<>>");
            }
            case "customer.updated" -> {
                log.info("<<>> Customer Updated successfully <<>>");
            }
            case "payment_intent.created" -> {
                log.info("<<>> Payment Intent Created <<>>");
            }
            case "payment_intent.succeeded" -> {
                log.info("<<>> Payment Intent Succeeded <<>>");
                event.getDataObjectDeserializer().getObject().ifPresent(stripeObject -> {
                    if (stripeObject instanceof PaymentIntent paymentIntent) {
                        String bookingIdStr = paymentIntent.getMetadata().get("Booking ID");
                        if (bookingIdStr != null) {
                            try {
                                Long bookingId = Long.parseLong(bookingIdStr);
                                bookingService.updateBookingAfterPayment(bookingId);
                            } catch (Exception e) {
                                log.error("Failed to update booking status after PaymentIntent succeeded: {}", e.getMessage(), e);
                            }
                        }
                    }
                });
            }
            case "price.created" -> {
                log.info("<<>> Payment Created Successfully <<>>");
            }
            case "price.deleted" -> {
                log.info("<<>> Price Created Successfully <<>>");
            }
            case "price.updated" -> {
                log.info("<<>> Price Updated Successfully <<>>");
            }
            case "product.created" -> {
                log.info("<<>> Product Created Successfully <<>>");
            }
            case "product.deleted" -> {
                log.info("<<>> Product Deleted Successfully <<>>");
            }
            case "product.updated" -> {
                log.info("<<>> Product Updated Successfully <<>>");
            }
            // ... handle other event types
            default -> log.info("<<>> Unhandled Event Type: {} <<>>", event.getType());
        }
    }
}
