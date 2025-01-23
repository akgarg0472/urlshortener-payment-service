package com.akgarg.paymentservice.eventpublisher.voidd;

import com.akgarg.paymentservice.eventpublisher.PaymentEvent;
import com.akgarg.paymentservice.eventpublisher.PaymentEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
@Slf4j
public class VoidPaymentEventPublisher implements PaymentEventPublisher {

    @Override
    public void publish(final PaymentEvent paymentEvent) {
        log.info("Publishing payment event {}", paymentEvent);
    }

}
