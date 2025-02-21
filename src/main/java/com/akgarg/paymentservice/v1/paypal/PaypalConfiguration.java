package com.akgarg.paymentservice.v1.paypal;

import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.authentication.ClientCredentialsAuthModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.util.Objects;

@Configuration
public class PaypalConfiguration {

    @Bean
    @Profile("prod")
    public PaypalServerSdkClient paypalDevHttpEnvironmentClient(final Environment environment) {
        final var paypalEnvironment = com.paypal.sdk.Environment.fromString(
                Objects.requireNonNull(environment.getProperty("paypal.environment"), "Environment property 'paypal.environment' is required")
        );

        final var clientCredentialsAuthModel = new ClientCredentialsAuthModel.Builder(
                Objects.requireNonNull(environment.getProperty("paypal.oauth.client-id"), "Environment property 'paypal.oauth.client-id' is required"),
                Objects.requireNonNull(environment.getProperty("paypal.oauth.client-secret"), "Environment property 'paypal.oauth.client-secret' is required")
        ).build();

        return new PaypalServerSdkClient.Builder()
                .httpClientConfig(configBuilder -> configBuilder.timeout(0))
                .clientCredentialsAuth(clientCredentialsAuthModel)
                .environment(paypalEnvironment)
                .build();
    }

    @Bean
    @Profile("dev")
    public PaypalServerSdkClient paypalServerSdkClient() {
        return new PaypalServerSdkClient.Builder()
                .build();
    }

}
