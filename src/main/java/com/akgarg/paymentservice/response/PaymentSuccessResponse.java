package com.akgarg.paymentservice.response;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
public record PaymentSuccessResponse(String clientSecret, int statusCode) implements PaymentResponse {

    @Override
    public String message() {
        return null;
    }

    @Override
    public String[] errors() {
        return new String[0];
    }

}
