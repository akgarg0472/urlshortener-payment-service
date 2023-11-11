package com.akgarg.paymentservice.paymentgameway;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
@Component
public class PaymentGatewayServiceFactory {

    private final Collection<PaymentGatewayService> paymentGatewayServices;

    public PaymentGatewayServiceFactory(final Collection<PaymentGatewayService> paymentGatewayServices) {
        this.paymentGatewayServices = paymentGatewayServices;
    }

    public Optional<PaymentGatewayService> getPaymentGatewayService(final String paymentGateway) {
        Objects.requireNonNull(paymentGateway, "Payment gateway cannot be null");

        for (PaymentGatewayService paymentGatewayService : paymentGatewayServices) {
            if (paymentGateway.equalsIgnoreCase(paymentGatewayService.getPaymentGateway())) {
                return Optional.of(paymentGatewayService);
            }
        }

        return Optional.empty();
    }

}
