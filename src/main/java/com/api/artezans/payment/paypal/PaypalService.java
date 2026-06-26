package com.api.artezans.payment.paypal;

import com.api.artezans.payment.paypal.dto.OrderDetail;
import com.paypal.api.payments.*;
import com.paypal.base.rest.PayPalRESTException;



public interface PaypalService {


    String authorizePayment(
            OrderDetail orderDetail, String cancelUrl, String successUrl
    ) throws PayPalRESTException;

    Payment executePayment(String paymentId, String payerId) throws PayPalRESTException;
}
