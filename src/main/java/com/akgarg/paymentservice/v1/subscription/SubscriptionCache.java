package com.akgarg.paymentservice.v1.subscription;

import java.util.Optional;

public interface SubscriptionCache {

    void addOrUpdateActiveSubscription(Subscription subscription);

    Optional<Subscription> getActiveSubscription(String userId);

    Optional<SubscriptionPack> getSubscriptionPack(String packId);

}
