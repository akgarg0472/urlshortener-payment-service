package com.akgarg.paymentservice.v1.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SubscriptionPack(
        @JsonProperty("id") String packId,
        @JsonProperty("price") Double price,
        @JsonProperty("default_pack") Boolean defaultPack
) {
}
