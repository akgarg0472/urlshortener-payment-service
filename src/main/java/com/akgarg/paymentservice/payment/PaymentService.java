package com.akgarg.paymentservice.payment;

import com.akgarg.paymentservice.request.CompletePaymentRequest;
import com.akgarg.paymentservice.request.CreatePaymentRequest;
import com.akgarg.paymentservice.request.VerifyPaymentRequest;
import com.akgarg.paymentservice.response.CompletePaymentResponse;
import com.akgarg.paymentservice.response.CreatePaymentResponse;
import com.akgarg.paymentservice.response.VerifyPaymentResponse;

public interface PaymentService {

    CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest);

    CompletePaymentResponse completePayment(CompletePaymentRequest completePaymentRequest);

    VerifyPaymentResponse verifyPayment(VerifyPaymentRequest verifyPaymentRequest);

    String getPaymentGatewayServiceName();

}
