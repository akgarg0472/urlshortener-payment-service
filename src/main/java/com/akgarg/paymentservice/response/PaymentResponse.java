package com.akgarg.paymentservice.response;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
public sealed interface PaymentResponse permits PaymentFailureResponse, PaymentSuccessResponse {

    String message();

    String[] errors();

    int statusCode();

}
