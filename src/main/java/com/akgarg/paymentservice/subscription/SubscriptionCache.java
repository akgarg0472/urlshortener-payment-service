package com.akgarg.paymentservice.subscription;

import java.util.Optional;

public interface SubscriptionCache {

    void populateCache();

    void addSubscription(Subscription subscription);

    void removeSubscription(Subscription subscription);

    void updateSubscription(Subscription subscription);

    Optional<Subscription> getSubscriptions(String userId);

    Optional<SubscriptionPack> getSubscriptionPlan(String packId);

}
