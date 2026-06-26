package com.api.artezans.gateway.paypal;

import com.api.artezans.payment.paypal.PaypalService;
import com.api.artezans.payment.paypal.dto.OrderDetail;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/paypal")
public class PaypalGateway {

    private final PaypalService paypalService;

    @Value("${success.url}")
    private String successUrl;

    @Value("${cancel.url}")
    private String cancelUrl;

    public static final String SUCCESS_URL = "pay/success";
    public static final String CANCEL_URL = "pay/cancel";


    @PostMapping("authorize")
    @Operation(summary = "To authorize payment")
    public ResponseEntity<String> authorizePayment(@RequestBody OrderDetail order) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(paypalService.authorizePayment(order, cancelUrl, successUrl));
        } catch (PayPalRESTException e) {
            log.error("PayPal authorization failed: {}", e.getMessage());
            throw new RuntimeException("Error making payment");
        }
    }

    @GetMapping(value = CANCEL_URL)
    public String cancelPay() {
        return "cancel";
    }

    @GetMapping(value = SUCCESS_URL)
    public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            log.info("Payment object: {}", payment.toJSON());
            if (payment.getState().equals("approved")) {
                return "success";
            }
        } catch (PayPalRESTException e) {
            log.error("PayPal payment execution failed: {}", e.getMessage(), e);
        }
        return "redirect:/";
    }

}
