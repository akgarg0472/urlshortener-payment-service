package com.akgarg.paymentservice.payment;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
public enum PaymentStatus {

    SUCCESS("success"),
    FAILED("failed"),
    PENDING("pending"),
    PROCESSING("processing"),
    CONFIRMATION_REQUIRED("confirmation_required");

    private final String value;

    PaymentStatus(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
