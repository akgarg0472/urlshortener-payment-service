package com.akgarg.paymentservice.eventpublisher;

public record PaymentEvent(
        String paymentId,
        String userId,
        String packId,
        String amount,
        String currency,
        String paymentGateway
) {
}
