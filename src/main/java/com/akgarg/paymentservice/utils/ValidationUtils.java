package com.akgarg.paymentservice.utils;

import com.akgarg.paymentservice.exception.PaymentException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
public final class ValidationUtils {

    private ValidationUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void checkAndThrowValidationException(final String requestId, final BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            final var errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new PaymentException(requestId,
                    HttpStatus.BAD_REQUEST.value(),
                    errors,
                    "Validation failed"
            );
        }
    }

}