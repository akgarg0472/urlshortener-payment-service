package com.akgarg.paymentservice.eventpublisher;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
public class VoidPaymentEventPublisher implements PaymentEventPublisher {

    @Override
    public void publish(final PaymentEvent paymentEvent) {
        // do nothing
    }

}
