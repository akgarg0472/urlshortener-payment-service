package com.akgarg.paymentservice.stripe;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
public record StripeCallbackParams(
        @JsonProperty("payment_intent") String paymentIntentId,
        @JsonProperty("redirect_status") String redirectStatus
) {
}
