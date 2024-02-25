package com.api.artezans.gateway.stripe;


import com.api.artezans.payment.StripeWebhook;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "Stripe Webhook")
@RequestMapping("api/v1/stripe")
public class StripeGateway {

    private final StripeWebhook stripeWebhook;


    @PostMapping("webhook")
    @Operation(summary = "The endpoint Stripe notifies when a registered event happens on our account with them")
    public void handlingEvents(@RequestBody Event event) throws StripeException {
        stripeWebhook.handlingEvents(event);
    }
}