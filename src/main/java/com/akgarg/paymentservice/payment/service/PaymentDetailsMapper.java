package com.akgarg.paymentservice.payment.service;

import com.akgarg.paymentservice.payment.PaymentDetail;
import com.akgarg.paymentservice.payment.PaymentStatus;
import com.akgarg.paymentservice.request.CreatePaymentRequest;
import jakarta.annotation.Nonnull;

import java.time.Instant;

final class PaymentDetailsMapper {

    private PaymentDetailsMapper() {
        throw new IllegalStateException();
    }

    static PaymentDetail from(
            @Nonnull final CreatePaymentRequest createPaymentRequest
    ) {
        final PaymentDetail paymentDetail = new PaymentDetail();
        paymentDetail.setUserId(createPaymentRequest.userId());
        paymentDetail.setPlanId(createPaymentRequest.packId());
        paymentDetail.setAmount(createPaymentRequest.amount());
        paymentDetail.setPaymentStatus(PaymentStatus.CREATED);
        paymentDetail.setCurrency(createPaymentRequest.currency());
        paymentDetail.setPaymentMethod(createPaymentRequest.paymentMethod());
        paymentDetail.setPaymentCreatedAt(Instant.now());
        paymentDetail.setPaymentCompletedAt(null);
        return paymentDetail;
    }

}
