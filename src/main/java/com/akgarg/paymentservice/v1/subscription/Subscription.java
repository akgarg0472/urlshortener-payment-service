package com.akgarg.paymentservice.v1.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;

import static com.akgarg.paymentservice.utils.PaymentServiceUtils.maskString;

public record Subscription(
        @JsonProperty("user_id") String userId,
        @JsonProperty("pack_id") String packId,
        @JsonProperty("expires_at") long expiresAt
) {

    @Override
    public String toString() {
        return "Subscription{" +
                "userId='" + maskString(userId) + '\'' +
                ", packId='" + maskString(packId) + '\'' +
                ", expiresAt=" + maskString(String.valueOf(expiresAt)) +
                '}';
    }

}
