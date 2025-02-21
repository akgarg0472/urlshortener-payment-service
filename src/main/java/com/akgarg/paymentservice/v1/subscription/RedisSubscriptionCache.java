package com.akgarg.paymentservice.v1.subscription;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class RedisSubscriptionCache implements SubscriptionCache {

    private static final String ACTIVE_SUBSCRIPTION_REDIS_KEY = "active_sub:";
    private static final String SUBSCRIPTION_PACK_REDIS_KEY = "sub_pack:";

    private final RedisTemplate<String, String> redisTemplate;
    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;
    private final Environment environment;

    @PostConstruct
    public void init() {
        log.info("Initializing RedisSubscriptionCache");
        populateCache();
    }

    @Override
    public void addOrUpdateActiveSubscription(final Subscription subscription) {
        log.info("Adding/updating subscription {}", subscription);
        final var expirationTime = Long.parseLong(environment.getProperty("subscription.cache.expiration.active-plan", "3_00_000"));

        try {
            redisTemplate.opsForValue().set(getActiveSubscriptionKey(subscription.userId()),
                    objectMapper.writeValueAsString(subscription),
                    Math.min(expirationTime, subscription.expiresAt() - System.currentTimeMillis()),
                    TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Error adding/updating subscription", e);
        }
    }

    @Override
    public Optional<Subscription> getActiveSubscription(final String userId) {
        Objects.requireNonNull(userId, "userId cannot be null");
        log.info("Getting active subscription for userId {}", userId);

        try {
            final var subscriptionObject = redisTemplate.opsForValue().get(getActiveSubscriptionKey(userId));

            if (subscriptionObject != null) {
                return Optional.ofNullable(objectMapper.readValue(subscriptionObject, Subscription.class));
            }

            final var activeSubscription = subscriptionService.getActiveSubscriptionForUser(userId);
            activeSubscription.ifPresent(this::addOrUpdateActiveSubscription);
            return activeSubscription;
        } catch (Exception e) {
            log.error("Error getting active subscription for userId {}", userId, e);
            throw new SubscriptionCacheException(e);
        }
    }

    @Override
    public Optional<SubscriptionPack> getSubscriptionPack(final String packId) {
        log.info("Getting subscription pack for packId {}", packId);

        try {
            final var cachedPack = redisTemplate.opsForValue().get(getSubscriptionPackKey(packId));
            if (cachedPack != null) {
                return Optional.ofNullable(objectMapper.readValue(cachedPack, SubscriptionPack.class));
            }
            final var subscriptionPack = subscriptionService.getSubscriptionPack(packId);
            if (subscriptionPack.isPresent()) {
                addOrUpdateSubscriptionPack(subscriptionPack.get());
            }
            return subscriptionPack;
        } catch (Exception e) {
            log.error("Error getting subscription pack for packId {}", packId, e);
            throw new SubscriptionCacheException(e);
        }
    }

    private void addOrUpdateSubscriptionPack(final SubscriptionPack pack) throws JsonProcessingException {
        log.info("Adding/updating subscription pack: {}", pack);

        final var expirationTime = Long.parseLong(environment.getProperty("subscription.cache.expiration.pack",
                "4_32_00_000"));

        redisTemplate.opsForValue().set(getSubscriptionPackKey(pack.packId()),
                objectMapper.writeValueAsString(pack),
                expirationTime,
                TimeUnit.MILLISECONDS);
    }

    private void populateCache() {
        log.info("Populating RedisSubscriptionCache");
        final var subscriptionPacks = subscriptionService.getSubscriptionPacks();

        for (final var pack : subscriptionPacks) {
            try {
                addOrUpdateSubscriptionPack(pack);
            } catch (Exception e) {
                log.error("Error while populating {} operation", SUBSCRIPTION_PACK_REDIS_KEY, e);
            }
        }
    }

    private String getSubscriptionPackKey(final String packId) {
        return SUBSCRIPTION_PACK_REDIS_KEY + packId;
    }

    private String getActiveSubscriptionKey(final String userId) {
        return ACTIVE_SUBSCRIPTION_REDIS_KEY + userId;
    }

}
