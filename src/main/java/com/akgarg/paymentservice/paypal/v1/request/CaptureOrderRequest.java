package com.akgarg.paymentservice.paypal.v1.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record CaptureOrderRequest(
        @NotBlank(message = "payment_id can't be null or empty")
        @JsonProperty("payment_id")
        String paymentId,

        @NotBlank(message = "payer_id can't be null or empty")
        @JsonProperty("payer_id")
        String payerId
) {
}
