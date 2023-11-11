package com.akgarg.paymentservice.stripe;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
@RestController
@RequestMapping("/payment")
public class StripeCallbackController {

    private final StripePaymentGatewayService stripePaymentGatewayService;

    public StripeCallbackController(final StripePaymentGatewayService stripePaymentGatewayService) {
        this.stripePaymentGatewayService = stripePaymentGatewayService;
    }

    @GetMapping("/stripe/callback")
    public ResponseEntity<StripeCallbackResponse> paymentSuccess(@RequestParam final Map<String, Object> stripeCallbackParams) {
        final StripeCallbackResponse stripeCallbackResponse = stripePaymentGatewayService.processCallback(stripeCallbackParams);
        return ResponseEntity
                .status(stripeCallbackResponse.statusCode())
                .location(URI.create(stripeCallbackResponse.redirectUrl()))
                .build();
    }

}
