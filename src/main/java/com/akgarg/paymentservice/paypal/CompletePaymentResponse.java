package com.akgarg.paymentservice.paypal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

record CompletePaymentResponse(
        @JsonIgnore int statusCode,
        @Nullable @JsonProperty("payment_id") String paymentId,
        @JsonProperty("payment_status") String paymentStatus,
        @JsonProperty("message") String message
) {
}
