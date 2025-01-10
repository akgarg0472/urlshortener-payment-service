package com.akgarg.paymentservice.subscription;

public record Subscription(
        String userId,
        String packId,
        long expiresAt
) {
}
