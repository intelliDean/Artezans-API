package com.api.artezans.gateway.stripe;

public interface StripeConstants {

    // Webhook handler
    String WEBHOOK_SUM = "Stripe webhook handler";
    String WEBHOOK_DESC = """
            When a registered event occurs on the Stripe account, Stripe sends a notification to this endpoint.
            The event is then processed and the appropriate action is taken.
            """;
    String WEBHOOK_OP_ID = "stripe.webhook";
}
