package com.akgarg.paymentservice.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@Profile({"prod", "PROD"})
@RequiredArgsConstructor
public class RedisSubscriptionCache implements SubscriptionCache {

    private static final String SUBSCRIPTION_SERVICE_NAME = "urlshortener-subscription-service";
    private static final String ACTIVE_SUBSCRIPTIONS_ENDPOINT = "/api/v1/subscriptions/active";
    private static final String SUBSCRIPTION_PACKS_ENDPOINT = "/api/v1/subscriptions/packs";

    private static final String ACTIVE_SUBSCRIPTION_REDIS_KEY = "activeSubscriptions";
    private static final String SUBSCRIPTION_PACK_REDIS_KEY = "subscriptionPacks";

    private final ExecutorService populateCacheExecutorService = Executors.newSingleThreadExecutor();

    private final RedisTemplate<String, Object> redisTemplate;
    private final RestClient.Builder restClientBuilder;
    private final DiscoveryClient discoveryClient;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        log.info("Initializing RedisSubscriptionCache");
        populateCache();
    }

    @Override
    public void populateCache() {
        populateCacheExecutorService.submit(() -> {
            final List<Subscription> activeSubscriptions = getActiveSubscriptions();
            final List<SubscriptionPack> subscriptionPacks = getSubscriptionPacks();
            final var hashOperations = redisTemplate.opsForHash();

            for (final Subscription subscription : activeSubscriptions) {
                try {
                    hashOperations.putIfAbsent(ACTIVE_SUBSCRIPTION_REDIS_KEY, subscription.userId(), subscription);
                } catch (Exception e) {
                    log.error("Error while populating {} operation", ACTIVE_SUBSCRIPTION_REDIS_KEY, e);
                }
            }

            for (final SubscriptionPack subscriptionPack : subscriptionPacks) {
                try {
                    hashOperations.putIfAbsent(SUBSCRIPTION_PACK_REDIS_KEY, subscriptionPack.packId(), subscriptionPack);
                } catch (Exception e) {
                    log.error("Error while populating {} operation", SUBSCRIPTION_PACK_REDIS_KEY, e);
                }
            }
        });
    }

    private List<SubscriptionPack> getSubscriptionPacks() {
        final List<SubscriptionPack> subscriptionPacks = new ArrayList<>();

        while (true) {
            final var subsServiceInstances = discoveryClient.getInstances(SUBSCRIPTION_SERVICE_NAME);
            boolean success = false;

            for (final var instance : subsServiceInstances) {
                final var scheme = instance.getScheme();
                final var host = instance.getHost();
                final var port = instance.getPort();

                try {
                    final var subscriptions = restClientBuilder.build()
                            .get()
                            .uri(uriBuilder -> uriBuilder
                                    .scheme(scheme)
                                    .host(host)
                                    .port(port)
                                    .path(SUBSCRIPTION_PACKS_ENDPOINT)
                                    .build())
                            .retrieve()
                            .toEntity(new ParameterizedTypeReference<List<SubscriptionPack>>() {
                            })
                            .getBody();

                    if (subscriptions != null) {
                        subscriptionPacks.addAll(subscriptions);
                        success = true;
                    }
                } catch (Exception e) {
                    log.error("Error while getting subscription packs from {}", instance.getUri(), e);
                }
            }

            if (success) {
                break;
            }
        }

        return subscriptionPacks;
    }

    private List<Subscription> getActiveSubscriptions() {
        final List<Subscription> activeSubscriptions = new ArrayList<>();

        while (true) {
            final var subsServiceInstances = discoveryClient.getInstances(SUBSCRIPTION_SERVICE_NAME);
            boolean success = false;

            for (final var instance : subsServiceInstances) {
                final var scheme = instance.getScheme();
                final var host = instance.getHost();
                final var port = instance.getPort();

                try {
                    final var subscriptions = restClientBuilder.build()
                            .get()
                            .uri(uriBuilder -> uriBuilder
                                    .scheme(scheme)
                                    .host(host)
                                    .port(port)
                                    .path(ACTIVE_SUBSCRIPTIONS_ENDPOINT)
                                    .build())
                            .retrieve()
                            .toEntity(new ParameterizedTypeReference<List<Subscription>>() {
                            })
                            .getBody();

                    if (subscriptions != null) {
                        activeSubscriptions.addAll(subscriptions);
                        success = true;
                    }
                } catch (Exception e) {
                    log.error("Error while getting active subscriptions from {}", instance.getUri(), e);
                }
            }

            if (success) {
                break;
            }
        }

        return activeSubscriptions;
    }

    @Override
    public void addSubscription(final Subscription subscription) {
        log.info("Adding subscription {}", subscription);

        try {
            redisTemplate.opsForHash().putIfAbsent(ACTIVE_SUBSCRIPTION_REDIS_KEY, subscription.userId(), subscription);
        } catch (Exception e) {
            log.error("Error while adding subscription {}", subscription, e);
        }
    }

    @Override
    public void removeSubscription(final Subscription subscription) {
        log.info("Removing subscription {}", subscription);

        try {
            redisTemplate.opsForHash().delete(ACTIVE_SUBSCRIPTION_REDIS_KEY, subscription.userId());
        } catch (Exception e) {
            log.error("Error while removing subscription {}", subscription, e);
        }
    }

    @Override
    public void updateSubscription(final Subscription subscription) {
        log.info("Updating subscription {}", subscription);

        try {
            redisTemplate.opsForHash().putIfAbsent(ACTIVE_SUBSCRIPTION_REDIS_KEY, subscription.userId(), subscription);
        } catch (Exception e) {
            log.error("Error while updating subscription {}", subscription, e);
        }
    }

    @Override
    public Optional<Subscription> getSubscriptions(final String userId) {
        log.info("Getting subscriptions for {}", userId);

        try {
            final var subscriptionObject = redisTemplate.opsForHash().get(ACTIVE_SUBSCRIPTION_REDIS_KEY, userId);
            if (subscriptionObject != null) {
                return Optional.ofNullable(objectMapper.readValue(subscriptionObject.toString(), Subscription.class));
            }
        } catch (Exception e) {
            log.error("Error while getting subscriptions for {}", userId, e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<SubscriptionPack> getSubscriptionPlan(final String packId) {
        log.info("Getting subscription pack for {}", packId);

        try {
            final var subscriptionObject = redisTemplate.opsForHash().get(SUBSCRIPTION_PACK_REDIS_KEY, packId);
            if (subscriptionObject != null) {
                return Optional.ofNullable(objectMapper.readValue(subscriptionObject.toString(), SubscriptionPack.class));
            }
        } catch (Exception e) {
            log.error("Error while getting subscription pack for {}", packId, e);
        }

        return Optional.empty();
    }

}
