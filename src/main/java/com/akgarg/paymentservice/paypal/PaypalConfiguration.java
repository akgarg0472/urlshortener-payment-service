package com.akgarg.paymentservice.paypal;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Objects;

@Configuration
public class PaypalConfiguration {

    @Bean
    @Profile("dev")
    public PayPalHttpClient paypalDevHttpEnvironmentClient() {
        final Dotenv dotenv = Dotenv.load();
        return new PayPalHttpClient(new PayPalEnvironment.Sandbox(
                Objects.requireNonNull(dotenv.get("PAYPAL_SAND_API_CLIENT_KEY")),
                Objects.requireNonNull(dotenv.get("PAYPAL_SAND_API_SECRET_KEY"))
        ));
    }

}
