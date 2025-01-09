package com.akgarg.paymentservice.eventpublisher;

public interface PaymentEventPublisher {

    void publish(PaymentEvent paymentEvent);

}
