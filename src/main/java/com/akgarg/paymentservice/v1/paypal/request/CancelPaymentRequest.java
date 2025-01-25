package com.akgarg.paymentservice.v1.paypal.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record CancelPaymentRequest(
        @JsonProperty("user_id") @NotBlank(message = "user_id can't be null or empty") String userId,
        @JsonProperty("payment_id") @NotBlank(message = "payment_id can't be null or empty") String paymentId
) {
}
