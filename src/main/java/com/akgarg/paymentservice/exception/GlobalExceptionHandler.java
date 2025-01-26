package com.akgarg.paymentservice.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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
@Slf4j
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(final BadRequestException badRequestException) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        badRequestException.errors()
                ));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentException(final PaymentException paymentException) {
        return ResponseEntity
                .status(paymentException.getStatusCode())
                .body(new ApiErrorResponse(
                        paymentException.getStatusCode(),
                        paymentException.getMessage(),
                        paymentException.getErrors()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(final Exception e) {
        log.error("Exception occurred", e);

        final var paymentFailureResponse = switch (e.getClass().getSimpleName()) {
            case "HttpRequestMethodNotSupportedException" -> new ApiErrorResponse(405, "Method not allowed", null);
            case "HttpMediaTypeNotSupportedException" -> new ApiErrorResponse(400, "Media type is not supported", null);
            case "HttpMessageNotReadableException" ->
                    new ApiErrorResponse(400, "Please provide valid request body", null);
            case "MissingServletRequestParameterException" -> {
                final var ex = (MissingServletRequestParameterException) e;
                yield new ApiErrorResponse(400,
                        "Parameter '%s' of type %s is missing".formatted(ex.getParameterName(), ex.getParameterType()),
                        null);
            }
            case "NoResourceFoundException" -> new ApiErrorResponse(
                    404,
                    "Not Found",
                    List.of("Requested resource not found: " + ((NoResourceFoundException) e).getResourcePath())
            );
            case "MissingRequestHeaderException" -> {
                final var ex = (MissingRequestHeaderException) e;
                yield new ApiErrorResponse(400, "Required Request Header '%s' is missing".formatted(ex.getHeaderName()), null);
            }
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
