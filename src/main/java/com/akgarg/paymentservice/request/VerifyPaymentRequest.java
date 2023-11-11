package com.akgarg.paymentservice.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
public record VerifyPaymentRequest(
        @NotBlank(message = "Payment id is required")
        @JsonProperty("payment_id")
        String paymentId,

        @NotBlank(message = "Payment gateway is required")
        @JsonProperty("payment_gateway")
        String paymentGateway
) {
}