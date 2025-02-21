package com.akgarg.paymentservice.v1.subscription;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@Profile("dev")
public class InMemorySubscriptionCache implements SubscriptionCache {

    private final Map<String, Subscription> subscriptions = new HashMap<>();

    @Override
    public void addOrUpdateActiveSubscription(final Subscription subscription) {
        log.info("Adding subscription {}", subscription);
        subscriptions.put(subscription.userId(), subscription);
    }

    @Override
    public Optional<Subscription> getActiveSubscription(final String userId) {
        log.info("Getting subscriptions for {}", userId);
        return Optional.ofNullable(subscriptions.get(userId));
    }

    @Override
    public Optional<SubscriptionPack> getSubscriptionPack(final String packId) {
        log.info("Getting subscription pack for {}", packId);
        return Optional.empty();
    }

}
