package com.api.artezans.gateway.stripe;


import com.api.artezans.payment.StripeWebhook;
import com.stripe.model.Event;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.api.artezans.gateway.stripe.StripeConstants.*;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "Stripe Webhook")
@RequestMapping("api/v1/stripe")
public class StripeGateway {

    private final StripeWebhook stripeWebhook;


    @PostMapping("webhook")
    @Operation(summary = WEBHOOK_SUM, description = WEBHOOK_DESC, operationId = WEBHOOK_OP_ID)
    public void handlingEvents(@RequestBody Event event) {
        stripeWebhook.handlingEvents(event);
    }
}