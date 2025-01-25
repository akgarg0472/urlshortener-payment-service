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
    public void addOrUpdateActiveSubscription(final String requestId, final Subscription subscription) {
        log.info("[{}] Adding/updating subscription {}", requestId, subscription);
        final var expirationTime = Long.parseLong(environment.getProperty("subscription.cache.expiration.active-plan", "3_00_000"));

        try {
            redisTemplate.opsForValue().set(getActiveSubscriptionKey(subscription.userId()),
                    objectMapper.writeValueAsString(subscription),
                    Math.min(expirationTime, subscription.expiresAt() - System.currentTimeMillis()),
                    TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("[{}] Error while adding/updating subscription {}", requestId, subscription, e);
        }
    }

    @Override
    public Optional<Subscription> getActiveSubscription(final String requestId, final String userId) {
        Objects.requireNonNull(userId, "userId cannot be null");
        log.info("[{}] Getting subscriptions for {}", requestId, userId);

        try {
            final var subscriptionObject = redisTemplate.opsForValue().get(getActiveSubscriptionKey(userId));

            if (subscriptionObject != null) {
                return Optional.ofNullable(objectMapper.readValue(subscriptionObject, Subscription.class));
            }

            final var activeSubscription = subscriptionService.getActiveSubscriptionForUser(requestId, userId);
            activeSubscription.ifPresent(sub -> addOrUpdateActiveSubscription(requestId, sub));
            return activeSubscription;
        } catch (Exception e) {
            log.error("[{}] Error while getting active subscription for {}", requestId, userId, e);
            throw new SubscriptionCacheException(e);
        }
    }

    @Override
    public Optional<SubscriptionPack> getSubscriptionPack(final String requestId, final String packId) {
        log.info("[{}] Getting subscription pack for {}", requestId, packId);

        try {
            final var cachedPack = redisTemplate.opsForValue().get(getSubscriptionPackKey(packId));
            if (cachedPack != null) {
                return Optional.ofNullable(objectMapper.readValue(cachedPack, SubscriptionPack.class));
            }
            final var subscriptionPack = subscriptionService.getSubscriptionPack(requestId, packId);
            if (subscriptionPack.isPresent()) {
                addOrUpdateSubscriptionPack(subscriptionPack.get());
            }
            return subscriptionPack;
        } catch (Exception e) {
            log.error("[{}] Error while getting subscription pack for {}", requestId, packId, e);
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
