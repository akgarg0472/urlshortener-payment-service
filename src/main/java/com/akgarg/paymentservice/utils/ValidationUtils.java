package com.akgarg.paymentservice.utils;

import com.akgarg.paymentservice.exception.PaymentException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import java.util.Collection;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
public final class ValidationUtils {

    private ValidationUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void checkAndThrowValidationException(final BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            final Collection<String> errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new PaymentException(HttpStatus.BAD_REQUEST.value(), errors, "Validation failed");
        }
    }

}