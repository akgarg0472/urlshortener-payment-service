package com.akgarg.paymentservice.payment.factory;

import com.akgarg.paymentservice.payment.PaymentService;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentServiceFactory {

    private final Map<String, PaymentService> paymentServices;

    public PaymentServiceFactory(final Collection<PaymentService> services) {
        this.paymentServices = services.stream()
                .collect(Collectors.toMap(
                        PaymentService::getPaymentGatewayServiceName,
                        Function.identity()
                ));
    }

    public Optional<PaymentService> getPaymentService(final String paymentGatewayName) {
        return Optional.ofNullable(paymentServices.get(paymentGatewayName));
    }

}
