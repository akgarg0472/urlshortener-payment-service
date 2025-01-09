package com.akgarg.paymentservice.exception;

import jakarta.annotation.Nullable;
import lombok.Getter;

import java.util.Collection;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
@Getter
public class PaymentException extends RuntimeException {

    private final String requestId;
    private final int statusCode;
    private final Collection<String> errors;

    public PaymentException(
            final String requestId,
            final int statusCode,
            @Nullable final Collection<String> errors,
            final String message
    ) {
        super(message);
        this.requestId = requestId;
        this.errors = errors;
        this.statusCode = statusCode;
    }

}
