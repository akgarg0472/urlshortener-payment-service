package com.akgarg.paymentservice.response;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
@SuppressWarnings("all")
public record PaymentFailureResponse(
        int statusCode,
        String message,
        String[] errors
) implements PaymentResponse {

}
