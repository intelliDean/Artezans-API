package com.api.artezans.payment.paypal;

import com.api.artezans.payment.paypal.dto.OrderDetail;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class PaypalService {

    private final APIContext apiContext;

    public String authorizePayment(
            OrderDetail orderDetail, String cancelUrl, String successUrl
    ) throws PayPalRESTException {

        Payer payer = getPayerInformation(orderDetail);
        RedirectUrls redirectUrls = getRedirectURLs(cancelUrl, successUrl);
        List<Transaction> listTransaction = getTransactionInformation(orderDetail);

        Payment requestPayment = new Payment();
        requestPayment.setTransactions(listTransaction)
                .setRedirectUrls(redirectUrls)
                .setPayer(payer)
                .setIntent("authorize");

        Payment approvedPayment = requestPayment.create(apiContext);

        return getApprovalLink(approvedPayment);

    }

    private Payer getPayerInformation(OrderDetail orderDetail) {

        PayerInfo payerInfo = new PayerInfo();
        payerInfo.setFirstName(orderDetail.getFirstName())
                .setLastName(orderDetail.getLastName())
                .setEmail(orderDetail.getEmail());
        Payer payer = new Payer();
        payer.setPayerInfo(payerInfo)
                .setPaymentMethod("paypal");
        return payer;
    }

    private RedirectUrls getRedirectURLs(String cancelUrl, String successUrl) {
        RedirectUrls redirectUrls = new RedirectUrls();

//        redirectUrls.setCancelUrl("http://localhost:8080/PaypalTest/cancel.html")
//                .setReturnUrl("http://localhost:8080/PaypalTest/review_payment");

        redirectUrls.setCancelUrl(cancelUrl)
                .setReturnUrl(successUrl);
        return redirectUrls;
    }

    private List<Transaction> getTransactionInformation(OrderDetail orderDetail) {
        Amount amount = new Amount();
        amount.setCurrency("AUD")
                .setTotal(String.format("%.2f", orderDetail.getTotal()));;

        List<Item> items = new ArrayList<>();

        Item item = getItem(orderDetail);
        items.add(item);

        ItemList itemList = new ItemList();
        itemList.setItems(items);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount)
                .setDescription(orderDetail.getServiceName())
                .setItemList(itemList);

        List<Transaction> listTransaction = new ArrayList<>();
        listTransaction.add(transaction);

        return listTransaction;
    }

    private static Item getItem(OrderDetail orderDetail) {
        Item item = new Item();
        item.setCurrency("AUD")
                .setName(orderDetail.getServiceName())
                .setQuantity("1")
                .setDescription("This payment is for " + orderDetail.getServiceName() + " service")
                .setName(orderDetail.getServiceName())
                .setPrice(String.format("%.2f", orderDetail.getTotal()));
        return item;
    }

    private String getApprovalLink(Payment approvedPayment) {
        List<Links> links = approvedPayment.getLinks();
        String approvalLink = null;

        for (Links link : links) {
            if (link.getRel().equalsIgnoreCase("approval_url")) {
                approvalLink = link.getHref();
                break;
            }
        }
        return approvalLink;
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        PaymentExecution paymentExecution = new PaymentExecution();
        Payment payment = new Payment();

        paymentExecution.setPayerId(payerId);
        payment.setId(paymentId);

        return payment.execute(apiContext, paymentExecution);
    }
}
