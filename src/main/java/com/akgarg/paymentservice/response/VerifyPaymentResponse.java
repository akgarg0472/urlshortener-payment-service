package com.akgarg.paymentservice.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
public record VerifyPaymentResponse(
        @JsonProperty("status_code") int statusCode,
        @JsonProperty("message") String message,
        @JsonProperty("payment_status") String paymentStatus) {
}