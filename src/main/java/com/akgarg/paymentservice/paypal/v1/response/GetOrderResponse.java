package com.akgarg.paymentservice.paypal.v1.response;

import com.akgarg.paymentservice.payment.PaymentDetailDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

public record GetOrderResponse(
        @JsonProperty("status_code") int statusCode,
        @JsonProperty("trace_id") String traceId,
        @JsonProperty("message") String message,
        @Nullable @JsonProperty("data") PaymentDetailDto paymentDetail
) {
}
