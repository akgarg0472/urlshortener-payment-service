package com.akgarg.paymentservice.paypal.v1.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(

        @NotBlank(message = "Please provide valid used_id")
        @JsonProperty("user_id") String userId,

        @NotBlank(message = "Please provide valid currency_code")
        @JsonProperty("currency_code") String currencyCode,

        @Min(value = 1, message = "Please provide valid amount")
        @JsonProperty("amount") Double amount,

        @NotBlank(message = "Please provide valid payment_method")
        @JsonProperty("payment_method") String paymentMethod,

        @NotBlank(message = "Please provide a valid pack_id")
        @JsonProperty("pack_id") String packId,

        @NotBlank(message = "Please provide valid description")
        @JsonProperty("description") String description

) {
}
