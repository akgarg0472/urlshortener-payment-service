package com.akgarg.paymentservice.exception;

import jakarta.annotation.Nullable;

import java.util.Collection;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
public class PaymentException extends RuntimeException {

    private final int statusCode;
    private final Collection<String> errors;

    public PaymentException(
            final int statusCode,
            @Nullable final Collection<String> errors,
            final String message
    ) {
        super(message);
        this.errors = errors;
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }

    @Nullable
    public Collection<String> errors() {
        return errors;
    }

}
