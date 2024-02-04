package com.akgarg.paymentservice.paypal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

import java.util.Collection;

record CreatePaymentResponse(
        @JsonProperty("trace_id") String traceId,
        @JsonProperty("message") String message,
        @JsonIgnore int statusCode,
        @Nullable @JsonProperty("payment_id") String orderId,
        @Nullable @JsonProperty("paypal_redirect_uri") String paypalRedirectUrl,
        @Nullable @JsonProperty("errors") Collection<String> errors
) {

    static CreatePaymentResponse error(final String traceId, final Collection<String> errors) {
        return new CreatePaymentResponse(traceId, "Internal Server Error", 500, null, null, errors);
    }

}