package com.akgarg.paymentservice.v1.paypal.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

import java.util.Collection;

public record CreateOrderResponse(
        @JsonProperty("status_code") int statusCode,
        @JsonProperty("trace_id") String traceId,
        @JsonProperty("message") String message,
        @Nullable @JsonProperty("payment_id") String orderId,
        @Nullable @JsonProperty("approval_url") String approvalUrl,
        @Nullable @JsonProperty("errors") Collection<String> errors
) {
}
