package com.akgarg.paymentservice.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

record CompletePaymentRequest(

        @NotBlank(message = "Please provide valid used id")
        @JsonProperty("user_id") String userId,

        @NotBlank(message = "Please provide valid payment id")
        @JsonProperty("payment_id") String paymentId,

        @NotBlank(message = "Please provide valid payerId")
        @JsonProperty("payer_id") String payerId

) {
}
