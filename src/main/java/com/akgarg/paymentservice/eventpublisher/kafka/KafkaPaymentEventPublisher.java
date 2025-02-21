package com.akgarg.paymentservice.eventpublisher.kafka;

import com.akgarg.paymentservice.eventpublisher.PaymentEvent;
import com.akgarg.paymentservice.eventpublisher.PaymentEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value(value = "${kafka.payment.success.topic.name}")
    private String paymentTopicName;

    @Override
    public void publishPaymentSuccess(final PaymentEvent paymentEvent) {
        log.info("Publishing payment success event: {}", paymentEvent);

        serializeEvent(paymentEvent)
                .ifPresent(eventJson -> kafkaTemplate.send(paymentTopicName, eventJson)
                        .whenComplete((result, throwable) -> {
                            if (throwable != null) {
                                log.error("Failed to send payment success event", throwable);
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("Kafka event successfully published: {}", result);
                                }
                            }
                        })
                );
    }

    private Optional<String> serializeEvent(final PaymentEvent paymentEvent) {
        try {
            return Optional.of(objectMapper.writeValueAsString(paymentEvent));
        } catch (Exception e) {
            log.error("Error serializing payment success event", e);
            return Optional.empty();
        }
    }

}
