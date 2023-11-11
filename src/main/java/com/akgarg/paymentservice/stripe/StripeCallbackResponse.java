package com.akgarg.paymentservice.stripe;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
public record StripeCallbackResponse(int statusCode, String redirectUrl) {

}
