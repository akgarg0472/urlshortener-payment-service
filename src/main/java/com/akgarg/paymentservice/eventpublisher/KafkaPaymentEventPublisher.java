package com.akgarg.paymentservice.eventpublisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Profile("prod")
@RequiredArgsConstructor
@Component
public class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private static final Logger LOGGER = LogManager.getLogger(KafkaPaymentEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value(value = "${kafka.payment.topic.name}")
    private String paymentTopicName;

    @Override
    public void publish(final PaymentEvent paymentEvent) {
        serializeEvent(paymentEvent)
                .ifPresent(eventJson -> kafkaTemplate.send(paymentTopicName, eventJson)
                        .whenComplete((s1, s2) -> LOGGER.info("{}  : {}", s1, s2)));
    }

    private Optional<String> serializeEvent(final PaymentEvent paymentEvent) {
        try {
            return Optional.of(objectMapper.writeValueAsString(paymentEvent));
        } catch (Exception e) {
            LOGGER.error("Error occurred while serializing payment event", e);
            return Optional.empty();
        }
    }

}
