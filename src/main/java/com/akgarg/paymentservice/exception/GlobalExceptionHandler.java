package com.akgarg.paymentservice.exception;

import com.akgarg.paymentservice.response.PaymentFailureResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Advice class to handle all exceptions thrown by the application
 * and return {@link PaymentFailureResponse} as response back to the client.
 *
 * @author Akhilesh Garg
 * @since 11/11/23
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<PaymentFailureResponse> handlePaymentException(final PaymentException paymentException) {
        return ResponseEntity.status(paymentException.getStatusCode())
                .body(new PaymentFailureResponse(
                              paymentException.getStatusCode(),
                              paymentException.getMessage(),
                              paymentException.getErrors()
                      )
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<PaymentFailureResponse> handleException(final Exception exception) {
        exception.printStackTrace();

        final PaymentFailureResponse paymentFailureResponse = switch (exception.getClass().getSimpleName()) {
            case "HttpRequestMethodNotSupportedException" ->
                    new PaymentFailureResponse(405, "Method not allowed", null);
            case "HttpMediaTypeNotSupportedException" ->
                    new PaymentFailureResponse(400, "Media type is not supported", null);
            case "HttpMessageNotReadableException" ->
                    new PaymentFailureResponse(400, "Please provide valid payment params", null);
            default -> new PaymentFailureResponse(500, "Internal server error", null);
        };

        return ResponseEntity.status(paymentFailureResponse.statusCode())
                .body(paymentFailureResponse);
    }

}
