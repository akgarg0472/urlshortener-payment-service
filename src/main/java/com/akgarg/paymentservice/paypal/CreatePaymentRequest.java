package com.akgarg.paymentservice.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePaymentRequest(

        @NotBlank(message = "Please provide valid used id")
        @JsonProperty("user_id") String userId,

        @NotBlank(message = "Please provide valid pack id")
        @JsonProperty("pack_id") String packId,

        @NotBlank(message = "Please provide valid currency")
        @JsonProperty("currency") String currency,

        @Min(value = 1, message = "Please provide valid amount")
        @JsonProperty("amount") Long amount,

        @NotBlank(message = "Please provide valid payment method")
        @JsonProperty("payment_method") String paymentMethod,

        @NotBlank(message = "Please provide valid payment description")
        @Size(min = 32, message = "Description should be of min length 32")
        @JsonProperty("description") String description
) {
}