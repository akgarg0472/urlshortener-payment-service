package com.akgarg.paymentservice.subscription;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@Profile({"dev", "DEV"})
public class InMemorySubscriptionCache implements SubscriptionCache {

    private final Map<String, Subscription> subscriptions = new HashMap<>();
    private final Map<String, SubscriptionPack> subscriptionPacks = new HashMap<>();

    @Override
    public void populateCache() {
        // do nothing, as in-memory cache do nothing
    }

    @Override
    public void addSubscription(final Subscription subscription) {
        log.info("Adding subscription {}", subscription);
        subscriptions.put(subscription.userId(), subscription);
    }

    @Override
    public void removeSubscription(final Subscription subscription) {
        log.info("Removing subscription {}", subscription);
        subscriptions.remove(subscription.userId());
    }

    @Override
    public void updateSubscription(final Subscription subscription) {
        log.info("Updating subscription {}", subscription);
        subscriptions.put(subscription.userId(), subscription);
    }

    @Override
    public Optional<Subscription> getSubscriptions(final String userId) {
        log.info("Getting subscriptions for {}", userId);
        return Optional.ofNullable(subscriptions.get(userId));
    }

    @Override
    public Optional<SubscriptionPack> getSubscriptionPlan(final String packId) {
        log.info("Getting subscription pack for {}", packId);
        return Optional.ofNullable(subscriptionPacks.get(packId));
    }

}
