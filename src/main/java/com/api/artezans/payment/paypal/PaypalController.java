package com.api.artezans.payment.paypal;

import com.api.artezans.payment.paypal.dto.OrderDetail;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("api/v1/paypal")
public class PaypalController {

    private final PaypalService service;

    public static final String SUCCESS_URL = "pay/success";
    public static final String CANCEL_URL = "pay/cancel";


    @PostMapping("authorize")
    @Operation(summary = "To authorize payment")
    public ResponseEntity<String> authorizePayment(@RequestBody OrderDetail order) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(service.authorizePayment(order,
                            "http://localhost:9090/" + CANCEL_URL,
                            "http://localhost:9090/" + SUCCESS_URL)
                    );
        } catch (PayPalRESTException e) {
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
            Payment payment = service.executePayment(paymentId, payerId);
            log.info("Payment object: {}", payment.toJSON());
            if (payment.getState().equals("approved")) {
                return "success";
            }
        } catch (PayPalRESTException e) {
            System.out.println(e.getMessage());
        }
        return "redirect:/";
    }

}
