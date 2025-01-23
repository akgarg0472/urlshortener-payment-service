package com.akgarg.paymentservice.v1.paypal.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CaptureOrderResponse(
        @JsonProperty("message") String message,
        @JsonProperty("status_code") int statusCode
) {
}
