package com.akgarg.paymentservice.eventpublisher.voidd;

import com.akgarg.paymentservice.eventpublisher.PaymentEvent;
import com.akgarg.paymentservice.eventpublisher.PaymentEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
public class VoidPaymentEventPublisher implements PaymentEventPublisher {

    @Override
    public void publishPaymentSuccess(final PaymentEvent paymentEvent) {
        log.info("Publishing payment event {}", paymentEvent);
    }

}
