package com.akgarg.paymentservice.eventpublisher;

public record PaymentEvent(
        String userId,
        String planId,
        Long amount,
        String currency,
        String paymentGateway
) {
}