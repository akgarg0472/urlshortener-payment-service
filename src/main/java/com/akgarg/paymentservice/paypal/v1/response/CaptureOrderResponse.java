package com.akgarg.paymentservice.paypal.v1.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CaptureOrderResponse(
        @JsonProperty("message") String message,
        @JsonProperty("status_code") int statusCode
) {
}
