package com.akgarg.paymentservice.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

public record CompletePaymentResponse(
        @JsonIgnore int statusCode,
        @Nullable @JsonProperty("payment_id") String paymentId,
        @JsonProperty("payment_status") String paymentStatus,
        @JsonProperty("message") String message
) {
}
