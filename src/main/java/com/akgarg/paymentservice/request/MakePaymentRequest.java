package com.akgarg.paymentservice.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @author Akhilesh Garg
 * @since 11/09/23
 */
public record MakePaymentRequest(
        @NotBlank(message = "Payment gateway is required")
        @JsonProperty("payment_gateway")
        String paymentGateway,

        @NotNull(message = "Payment amount is required")
        @Min(value = 100, message = "Payment amount must be greater than 100")
        @JsonProperty("amount")
        Long amount,

        @NotBlank(message = "Payment currency is required")
        @JsonProperty("currency")
        String currency,

        @NotBlank(message = "Payment mode is required")
        @JsonProperty("payment_method")
        String paymentMethod,

        @NotBlank(message = "Payment description is required")
        @JsonProperty("description")
        String paymentDescription,

        @NotBlank(message = "Payment userId is required")
        @JsonProperty("user_id")
        String userId,

        @NotBlank(message = "Payment receipt email is required")
        @JsonProperty("receipt_email")
        String receiptEmail) {
}