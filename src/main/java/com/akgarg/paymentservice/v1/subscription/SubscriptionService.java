package com.akgarg.paymentservice.v1.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
@SuppressWarnings("DuplicatedCode")
public class SubscriptionService {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    private static final String SUBSCRIPTION_SERVICE_NAME = "urlshortener-subscription-service";
    private static final String SUBSCRIPTION_PACKS_ENDPOINT = "/api/v1/subscriptions/packs";
    private static final String ACTIVE_SUBSCRIPTION_ENDPOINT = "/api/v1/subscriptions/active";

    private final RestClient.Builder restClientBuilder;
    private final DiscoveryClient discoveryClient;
    private final Environment environment;

    public List<SubscriptionPack> getSubscriptionPacks() {
        log.info("Getting all subscription packs");
        final var instances = discoveryClient.getInstances(SUBSCRIPTION_SERVICE_NAME);

        for (final var instance : instances) {
            final var scheme = instance.getScheme();
            final var host = instance.getHost();
            final var port = instance.getPort();

            final var applicationName = environment.getProperty("spring.application.name", "urlshortener-payment-service");
            final var applicationPort = environment.getProperty("local.server.port", "null");
            final var requestIdHeader = applicationName + ":" + applicationPort;

            final var subscriptions = restClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme(scheme)
                            .host(host)
                            .port(port)
                            .path(SUBSCRIPTION_PACKS_ENDPOINT)
                            .queryParam("page", 0)
                            .queryParam("limit", 1000)
                            .build())
                    .header(REQUEST_ID_HEADER, requestIdHeader)
                    .retrieve()
                    .toEntity(SubscriptionPacksResponse.class)
                    .getBody();

            if (subscriptions != null) {
                return subscriptions.packs;
            }
        }

        return Collections.emptyList();
    }

    Optional<Subscription> getActiveSubscriptionForUser(final String requestId, final String userId) {
        log.info("[{}] Getting active subscription for user {}", requestId, userId);

        final var subsServiceInstances = discoveryClient.getInstances(SUBSCRIPTION_SERVICE_NAME);

        for (final var instance : subsServiceInstances) {
            final var scheme = instance.getScheme();
            final var host = instance.getHost();
            final var port = instance.getPort();

            final var activeSubscriptionResponse = restClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme(scheme)
                            .host(host)
                            .port(port)
                            .path(ACTIVE_SUBSCRIPTION_ENDPOINT)
                            .queryParam("userId", userId)
                            .build())
                    .retrieve()
                    .toEntity(ActiveSubscriptionResponse.class)
                    .getBody();

            if (activeSubscriptionResponse != null) {
                return Optional.ofNullable(activeSubscriptionResponse.subscription);
            }
        }

        return Optional.empty();
    }

    public Optional<SubscriptionPack> getSubscriptionPack(final String requestId, final String packId) {
        log.info("[{}] Getting subscription pack {}", requestId, packId);

        final var instances = discoveryClient.getInstances(SUBSCRIPTION_SERVICE_NAME);

        for (final var instance : instances) {
            final var scheme = instance.getScheme();
            final var host = instance.getHost();
            final var port = instance.getPort();

            final var applicationName = environment.getProperty("spring.application.name", "urlshortener-payment-service");
            final var applicationPort = environment.getProperty("local.server.port", "null");
            final var requestIdHeader = applicationName + ":" + applicationPort;

            final var subscriptionPack = restClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme(scheme)
                            .host(host)
                            .port(port)
                            .pathSegment(SUBSCRIPTION_PACKS_ENDPOINT, packId)
                            .build())
                    .header(REQUEST_ID_HEADER, requestIdHeader)
                    .retrieve()
                    .toEntity(SubscriptionPackResponse.class)
                    .getBody();

            log.debug("[{}] Subscription pack response: {}", requestId, subscriptionPack);

            if (subscriptionPack != null) {
                return Optional.ofNullable(subscriptionPack.pack);
            }
        }

        return Optional.empty();
    }

    record SubscriptionPacksResponse(@JsonProperty("packs") List<SubscriptionPack> packs) {
    }

    record SubscriptionPackResponse(@JsonProperty("pack") SubscriptionPack pack) {
    }

    record ActiveSubscriptionResponse(@JsonProperty("subscription") Subscription subscription) {
    }

}
