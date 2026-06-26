package com.api.artezans.gateway.booking;

public interface BookingUtil {

    // Book a service
    String BOOK_SUMMARY = "Book a service";
    String BOOK_DESCRIPTION = """
            When a customer or service provider wants to book for a service,
            they do that through this endpoint.  (tested ✅)
            """;
    String BOOK_OP_ID = "book.service";

    // Accept proposal
    String ACCEPT_PROPOSAL_SUM = "Accept booking proposal";
    String ACCEPT_PROPOSAL_DESC = """
            After a customer has made a proposal to a service provider and the service provider
            wants to accept it, they do that through this endpoint.
            """;
    String ACCEPT_PROPOSAL_OP_ID = "accept.proposal";

    // Reject proposal
    String REJECT_PROPOSAL_SUM = "Reject booking proposal";
    String REJECT_PROPOSAL_DESC = """
            After a customer has made a proposal to a service provider and the service provider
            wants to reject it, they do that through this endpoint.  (tested ✅)
            """;
    String REJECT_PROPOSAL_OP_ID = "reject.proposal";

    // Complete task
    String COMPLETE_SUMMARY = "Service provider completes task";
    String COMPLETE_DESCRIPTION = """
            When the service provider is done with a task, they call this endpoint to mark it complete
            and notify the customer of the completion.  (tested ✅)
            """;
    String COMPLETE_OP_ID = "complete.task";

    // Stripe payment intent
    String INTENT_SUMMARY = "Initiate payment intent for Stripe";
    String INTENT_DESCRIPTION = """
            For a customer to make payment via Stripe, they call this endpoint to create a payment intent
            before invoking the Stripe SDK for the actual charge.  (tested ✅)
            """;
    String INTENT_OP_ID = "payment.intent";

    // PayPal authorise
    String AUTHORIZE_SUMMARY = "PayPal authorise payment";
    String AUTHORIZE_DESCRIPTION = """
            When the customer is about to make payment, they first need their payment authorised
            using this endpoint.  (tested ✅)
            """;
    String AUTHORIZE_OP_ID = "paypal.authorize";

    // PayPal execute
    String EXECUTE_SUMMARY = "Execute PayPal payment after authorisation";
    String EXECUTE_DESCRIPTION = "Once the payment is authorised, it is executed through this endpoint.";
    String EXECUTE_OP_ID = "execute.paypal";

    // Send notification after payment
    String NOTIFY_SUMMARY = "Send notification after payment";
    String NOTIFY_DESCRIPTION = "When a customer pays for a service, the service provider is notified via this endpoint.";
    String NOTIFY_OP_ID = "notification.payment";

    // Customer accepts service
    String ACCEPT_SERVICE_SUM = "Customer accepts service";
    String ACCEPT_SERVICE_DESC = """
            When the service provider notifies the customer of task completion and the customer
            is satisfied with the service, they call this endpoint to accept it.  (tested ✅)
            """;
    String ACCEPT_SERVICE_OP_ID = "accept.service";

    // Customer rejects service
    String REJECT_SERVICE_SUM = "Customer rejects service";
    String REJECT_SERVICE_DESC = """
            When the service provider notifies the customer of task completion and the customer
            is not satisfied, they call this endpoint to reject the service.  (tested ✅)
            """;
    String REJECT_SERVICE_OP_ID = "reject.service";

    // Update booking after payment
    String UPDATE_PAYMENT_SUM = "Update booking after payment is confirmed";
    String UPDATE_PAYMENT_DESC = """
            When the payment gateway confirms a customer's payment on a booking,
            the booking's stage is updated to PAID using this endpoint.
            """;
    String UPDATE_PAYMENT_OP_ID = "update.payment";

    // Generate invoice
    String GENERATE_INVOICE_SUM = "Service provider generates invoice";
    String GENERATE_INVOICE_DESC = "For a service provider to generate an invoice, they call this endpoint.";
    String GENERATE_INVOICE_OP_ID = "invoice.generate";

    // Stripe webhook (moved to StripeGateway but kept here for historical reference)
    String STRIPE_SUMMARY = "Stripe webhook";
    String STRIPE_DESCRIPTION = """
            When an event takes place on the Stripe account, Stripe sends the event here.
            This endpoint identifies the event type.
            """;
    String STRIPE_OP_ID = "stripe.webhook";
}
