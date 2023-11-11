package com.akgarg.paymentservice.payment;

import com.akgarg.paymentservice.exception.PaymentException;
import com.akgarg.paymentservice.paymentgameway.PaymentGatewayService;
import com.akgarg.paymentservice.paymentgameway.PaymentGatewayServiceFactory;
import com.akgarg.paymentservice.request.MakePaymentRequest;
import com.akgarg.paymentservice.request.VerifyPaymentRequest;
import com.akgarg.paymentservice.response.PaymentResponse;
import com.akgarg.paymentservice.response.VerifyPaymentResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.akgarg.paymentservice.utils.ValidationUtils.checkAndThrowValidationException;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
@RestController
public class PaymentController {

    private final PaymentGatewayServiceFactory paymentGatewayServiceFactory;

    public PaymentController(final PaymentGatewayServiceFactory paymentGatewayServiceFactory) {
        this.paymentGatewayServiceFactory = paymentGatewayServiceFactory;
    }

    @PostMapping("/payment")
    @CrossOrigin(origins = "*")
    public ResponseEntity<PaymentResponse> makePayment(
            @Valid @RequestBody final MakePaymentRequest paymentRequest,
            final BindingResult bindingResult
    ) {
        checkAndThrowValidationException(bindingResult);
        final PaymentGatewayService paymentGatewayService = getPaymentGatewayService(paymentRequest.paymentGateway());
        final PaymentResponse paymentResponse = paymentGatewayService.makePayment(paymentRequest);
        return ResponseEntity
                .status(paymentResponse.statusCode())
                .body(paymentResponse);
    }

    @PostMapping("/payment/verify")
    @CrossOrigin(origins = "*")
    public ResponseEntity<VerifyPaymentResponse> verifyPaymentStatus(
            @Valid @RequestBody final VerifyPaymentRequest verifyPaymentRequest,
            final BindingResult bindingResult
    ) {
        checkAndThrowValidationException(bindingResult);
        final PaymentGatewayService paymentGatewayService = getPaymentGatewayService(verifyPaymentRequest.paymentGateway());
        final VerifyPaymentResponse verifyPaymentResponse = paymentGatewayService.verifyPayment(verifyPaymentRequest);
        return ResponseEntity
                .status(verifyPaymentResponse.statusCode())
                .body(verifyPaymentResponse);
    }

    private PaymentGatewayService getPaymentGatewayService(final String paymentGateway) {
        final var paymentGatewayService = paymentGatewayServiceFactory.getPaymentGatewayService(paymentGateway);

        if (paymentGatewayService.isEmpty()) {
            throw new PaymentException(400, null, "Invalid payment gateway provided: " + paymentGateway);
        }

        return paymentGatewayService.get();
    }

}
