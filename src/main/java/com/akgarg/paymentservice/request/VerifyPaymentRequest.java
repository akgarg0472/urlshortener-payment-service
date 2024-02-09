package com.akgarg.paymentservice.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record VerifyPaymentRequest(

        @NotBlank(message = "Please provide valid payment gateway")
        @JsonProperty("payment_gateway") String paymentGateway,

        @NotBlank(message = "Please provide valid used id")
        @JsonProperty("user_id") String userId,

        @NotBlank(message = "Please provide valid payment id")
        @JsonProperty("payment_id") String paymentId

) {
}
