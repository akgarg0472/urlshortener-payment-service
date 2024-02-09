package com.akgarg.paymentservice.v1.razorpay;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Objects;

@Configuration
public class RazorpayConfiguration {

    @Bean
    @Profile("dev")
    public RazorpayClient devRazorpayClient() throws RazorpayException {
        final Dotenv dotenv = Dotenv.load();
        return new RazorpayClient(
                Objects.requireNonNull(dotenv.get("RAZORPAY_SAND_API_KEY"), "Razorpay Sand API key not found"),
                Objects.requireNonNull(dotenv.get("RAZORPAY_SAND_API_SECRET"), "Razorpay Sand API secret not found"),
                true
        );
    }

}
