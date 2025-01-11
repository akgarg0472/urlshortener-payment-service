package com.akgarg.paymentservice.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Subscription(
        @JsonProperty("user_id") String userId,
        @JsonProperty("pack_id") String packId,
        @JsonProperty("expires_at") long expiresAt
) {
}
