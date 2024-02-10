package com.akgarg.paymentservice.payment.service;

import com.akgarg.paymentservice.request.CompletePaymentRequest;
import com.akgarg.paymentservice.request.CreatePaymentRequest;
import com.akgarg.paymentservice.request.VerifyPaymentRequest;
import com.akgarg.paymentservice.response.CompletePaymentResponse;
import com.akgarg.paymentservice.response.CreatePaymentResponse;
import com.akgarg.paymentservice.response.VerifyPaymentResponse;
import jakarta.annotation.Nonnull;

public interface PaymentService {

    CreatePaymentResponse createPayment(@Nonnull CreatePaymentRequest createPaymentRequest);

    CompletePaymentResponse completePayment(@Nonnull CompletePaymentRequest completePaymentRequest);

    VerifyPaymentResponse verifyPayment(@Nonnull VerifyPaymentRequest verifyPaymentRequest);

    String getPaymentGatewayName();

}
