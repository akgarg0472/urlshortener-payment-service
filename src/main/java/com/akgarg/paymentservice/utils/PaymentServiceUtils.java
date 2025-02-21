package com.akgarg.paymentservice.utils;

import com.akgarg.paymentservice.exception.PaymentException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import java.util.Random;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
public final class PaymentServiceUtils {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String USER_ID_HEADER = "X-User-ID";
    public static final String REQUEST_ID_THREAD_CONTEXT_KEY = "requestId";

    private static final Random random = new Random();

    private PaymentServiceUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void checkAndThrowValidationException(final BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            final var errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new PaymentException(
                    HttpStatus.BAD_REQUEST.value(),
                    errors,
                    "Validation failed"
            );
        }
    }

    public static String maskString(final String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        final var length = input.length();
        final var minMaskCount = (int) Math.ceil(length * 0.4);
        final var maskedArray = input.toCharArray();
        final var maskedIndices = new boolean[length];
        int maskedCount = 0;

        while (maskedCount < minMaskCount) {
            var index = random.nextInt(length);
            if (!maskedIndices[index]) {
                maskedArray[index] = '*';
                maskedIndices[index] = true;
                maskedCount++;
            }
        }

        return new String(maskedArray);
    }

}