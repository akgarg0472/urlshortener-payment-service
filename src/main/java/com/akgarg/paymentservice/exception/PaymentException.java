package com.akgarg.paymentservice.exception;

import jakarta.annotation.Nullable;
import lombok.Getter;

import java.util.Collection;

@Getter
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

}
