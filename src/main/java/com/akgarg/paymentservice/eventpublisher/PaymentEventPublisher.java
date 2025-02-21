package com.akgarg.paymentservice.eventpublisher;

public interface PaymentEventPublisher {

    void publishPaymentSuccess(PaymentEvent paymentEvent);

}
