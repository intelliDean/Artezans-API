package com.api.artezans.gateway.booking;

public class BookingUtil {
    public final static String STRIPE_DESCRIPTION = "When an event takes place on our stripe account, Stripe sends " +
            "the event to us. This endpoint tell us what the event type is";
    public final static String STRIPE_SUMMARY = "Stripe webhook";
    public final static String STRIPE_OP_ID = "stripe.webhook";
    public final static String BOOK_DESCRIPTION = "When both customer and service provider wants to book for a service of a " +
            "service provider, they do that through this endpoint.  (tested✅)";
    public final static String BOOK_SUMMARY = "Book a service";
    public final static String BOOK_OP_ID = "book.service";
    public final static String ACCEPT_PROPOSAL_DESC = "After the service provider or customer had made a proposal to a service provider for " +
            "their service and the service provider want to accept the proposal, he does that through this endpoint";
    public final static String ACCEPT_PROPOSAL_SUM = "accept booking proposal";
    public final static String ACCEPT_PROPOSAL_OP_ID = "accept.proposal";
    public final static String REJECT_PROPOSAL_DESC = "After the service provider or customer had made a proposal to a service " +
            "provider for their service and the service provider want to reject the proposal, " +
            "he does that through this endpoint. (tested✅)";
    public final static String REJECT_PROPOSAL_SUM = "reject booking proposal";
    public final static String REJECT_PROPOSAL_OP_ID = "reject.proposal";
    public final static String COMPLETE_DESCRIPTION = "When the service provider is done with a task, he calls this endpoint to complete the" +
            "task and also inform the customer of the completion of the task. (tested✅)";
    public final static String COMPLETE_SUMMARY = "Service Provider completes task";
    public final static String COMPLETE_OP_ID = "complete.task";
    public final static String INTENT_DESCRIPTION = "For a customer to make payment, he calls this endpoint to make the payment" +
            "intent before calling Stripe SDK for the actual payment. (tested✅)";
    public final static String INTENT_SUMMARY = "Initiate Payment Intent for Stripe";
    public final static String INTENT_OP_ID = "payment.intent";
    public final static String AUTHORIZE_DESCRIPTION = "When the user is to make payment, he first need his payment to be" +
            " authorized using this endpoint.  (tested✅)";
    public final static String AUTHORIZE_SUMMARY = "Paypal authorize payment";
    public final static String AUTHORIZE_OP_ID = "paypal.authorize";
    public final static String EXECUTE_DESCRIPTION = "When user payment is authorized, the payment is executed using this endpoint";
    public final static String EXECUTE_SUMMARY = "Execute paypal payment after being authorized";
    public final static String EXECUTE_OP_ID = "execute.paypal";
    public final static String NOTIFY_DESCRIPTION = "When customer pays for a service, service provider is notified using this endpoint";
    public final static String NOTIFY_SUMMARY = "Sends notification after payment";
    public final static String NOTIFY_OP_ID = "notification.payment";
    public final static String ACCEPT_SERVICE_DESC = "When service provider notifies customer of the completion of his task and the customer" +
            "is satisfied with service, he calls this endpoint to accept the service. (tested✅)";
    public final static String ACCEPT_SERVICE_SUM = "Customer accepts service provider service";
    public final static String ACCEPT_SERVICE_OP_ID = "accept.service";
    public final static String REJECT_SERVICE_DESC = "When service provider notifies customer of the completion of his task and the customer" +
            "is not satisfied with service, he calls this endpoint to reject the service. (tested✅)";
    public final static String REJECT_SERVICE_SUM = "Customer rejects service provider service";
    public final static String REJECT_SERVICE_OP_ID = "reject.service";
    public final static String UPDATE_PAYMENT_DESC =  "When payment gateway alerts the system of a customer payment on a booking," +
            "the booking's state is updated using this endpoint";
    public final static String UPDATE_PAYMENT_SUM = "Update booking after payment is confirmed";
    public final static String UPDATE_PAYMENT_OP_ID = "update.payment";
    public final static String GENERATE_INVOICE_DESC =  "For service provider to generate invoice, he does that calling this endpoint";
    public final static String GENERATE_INVOICE_SUM = "Service provider generates invoice";
    public final static String GENERATE_INVOICE_OP_ID = "invoice.generate";


}
