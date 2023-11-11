package com.akgarg.paymentservice.exception;

import jakarta.annotation.Nullable;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
public class PaymentException extends RuntimeException {

    private final int statusCode;

    @Nullable
    private final String[] errors;

    public PaymentException(final int statusCode, @Nullable final String[] errors, final String message) {
        super(message);
        this.errors = errors;
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String[] getErrors() {
        return errors;
    }

}
