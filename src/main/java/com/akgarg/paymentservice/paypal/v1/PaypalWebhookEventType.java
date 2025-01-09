package com.akgarg.paymentservice.paypal.v1;

import java.util.Optional;

public enum PaypalWebhookEventType {

    ORDER_APPROVED("CHECKOUT.ORDER.APPROVED"),
    PAYMENT_CAPTURE_COMPLETE("PAYMENT.CAPTURE.COMPLETED");

    private final String value;

    PaypalWebhookEventType(final String value) {
        this.value = value;
    }

    public static Optional<PaypalWebhookEventType> fromValue(final String value) {
        for (final var type : PaypalWebhookEventType.values()) {
            if (type.value.equals(value)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    public String value() {
        return value;
    }

}
