package com.akgarg.paymentservice.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public record VerifyPaymentResponse(
        @JsonProperty("trace_id") String traceId,
        @JsonProperty("message") String message,
        @JsonIgnore int statusCode,
        @JsonProperty("payment_status") String paymentStatus
) {
}
