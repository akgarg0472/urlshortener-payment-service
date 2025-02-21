package com.akgarg.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class UrlShortenerPaymentService {

    public static void main(final String[] args) {
        SpringApplication.run(UrlShortenerPaymentService.class, args);
    }

}
