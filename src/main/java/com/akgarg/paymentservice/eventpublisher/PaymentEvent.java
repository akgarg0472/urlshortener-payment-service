package com.akgarg.paymentservice.eventpublisher;

public record PaymentEvent(
        String paymentId,
        String userId,
        String planId,
        Double amount,
        String currency,
        String paymentGateway
) {
}
