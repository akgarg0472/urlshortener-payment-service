package com.akgarg.paymentservice.v1.subscription;

import java.util.Optional;

public interface SubscriptionCache {

    void addOrUpdateActiveSubscription(String requestId, Subscription subscription);

    Optional<Subscription> getActiveSubscription(String requestId, String userId);

    Optional<SubscriptionPack> getSubscriptionPack(String requestId, String packId);

}
