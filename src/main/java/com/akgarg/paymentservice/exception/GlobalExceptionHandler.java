package com.akgarg.paymentservice.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Collection;
import java.util.List;

/**
 * Advice class to handle all exceptions thrown by the application
 *
 * @author Akhilesh Garg
 * @since 11/11/23
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentException(final PaymentException paymentException) {
        return ResponseEntity
                .status(paymentException.statusCode())
                .body(new ApiErrorResponse(
                        paymentException.statusCode(),
                        paymentException.getMessage(),
                        paymentException.errors()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(final Exception exception) {
        exception.printStackTrace();

        final ApiErrorResponse paymentFailureResponse = switch (exception.getClass().getSimpleName()) {
            case "HttpRequestMethodNotSupportedException" -> new ApiErrorResponse(405, "Method not allowed", null);
            case "HttpMediaTypeNotSupportedException" -> new ApiErrorResponse(400, "Media type is not supported", null);
            case "HttpMessageNotReadableException" ->
                    new ApiErrorResponse(400, "Please provide valid payment params", null);
            case "NoResourceFoundException" -> new ApiErrorResponse(
                    404,
                    "Not Found",
                    List.of("Requested resource not found: " + ((NoResourceFoundException) exception).getResourcePath())
            );
            default -> new ApiErrorResponse(500, "Internal server error", null);
        };

        return ResponseEntity.status(paymentFailureResponse.statusCode()).body(paymentFailureResponse);
    }

    record ApiErrorResponse(
            @JsonIgnore int statusCode,
            String message,
            Collection<String> errors
    ) {
    }

}
