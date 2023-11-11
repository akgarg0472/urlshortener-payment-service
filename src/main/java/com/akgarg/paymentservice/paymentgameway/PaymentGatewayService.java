package com.akgarg.paymentservice.paymentgameway;

import com.akgarg.paymentservice.request.MakePaymentRequest;
import com.akgarg.paymentservice.request.VerifyPaymentRequest;
import com.akgarg.paymentservice.response.PaymentResponse;
import com.akgarg.paymentservice.response.VerifyPaymentResponse;

/**
 * @author Akhilesh Garg
 * @since 11/09/23
 */
public interface PaymentGatewayService {

    PaymentResponse makePayment(MakePaymentRequest paymentRequest);

    VerifyPaymentResponse verifyPayment(VerifyPaymentRequest verifyPaymentRequest);

    String getPaymentGateway();

}
