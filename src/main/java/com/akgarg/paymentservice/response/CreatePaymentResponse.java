package com.akgarg.paymentservice.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

import java.util.Collection;

public record CreatePaymentResponse(
        @JsonProperty("trace_id") String traceId,
        @JsonProperty("message") String message,
        @JsonIgnore int statusCode,
        @Nullable @JsonProperty("payment_id") String orderId,
        @Nullable @JsonProperty("redirect_uri") String redirectUrl,
        @Nullable @JsonProperty("errors") Collection<String> errors
) {
}
