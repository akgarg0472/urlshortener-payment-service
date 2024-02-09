package com.akgarg.paymentservice.payment;

import com.akgarg.paymentservice.exception.BadRequestException;
import com.akgarg.paymentservice.payment.factory.PaymentServiceFactory;
import com.akgarg.paymentservice.request.CompletePaymentRequest;
import com.akgarg.paymentservice.request.CreatePaymentRequest;
import com.akgarg.paymentservice.request.VerifyPaymentRequest;
import com.akgarg.paymentservice.response.CompletePaymentResponse;
import com.akgarg.paymentservice.response.CreatePaymentResponse;
import com.akgarg.paymentservice.response.VerifyPaymentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.akgarg.paymentservice.utils.ValidationUtils.checkAndThrowValidationException;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final String BAD_PG_MSG = "Bad payment gateway: %s";

    private final PaymentServiceFactory paymentServiceFactory;

    public PaymentController(final PaymentServiceFactory paymentServiceFactory) {
        this.paymentServiceFactory = paymentServiceFactory;
    }

    @PostMapping("/create-payment")
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @Valid @RequestBody final CreatePaymentRequest createPaymentRequest,
            final BindingResult validationResult
    ) {
        checkAndThrowValidationException(validationResult);
        final var gatewayName = createPaymentRequest.paymentGateway();
        final var paymentService = paymentServiceFactory.getPaymentService(gatewayName)
                .orElseThrow(() -> new BadRequestException(List.of(BAD_PG_MSG.formatted(gatewayName))));
        final CreatePaymentResponse paymentResponse = paymentService.createPayment(createPaymentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentResponse);
    }

    @PostMapping("complete-payment")
    public ResponseEntity<CompletePaymentResponse> completePayment(
            @Valid @RequestBody final CompletePaymentRequest completePaymentRequest,
            final BindingResult validationResult
    ) {
        checkAndThrowValidationException(validationResult);
        final var gatewayName = completePaymentRequest.paymentGateway();
        final var paymentService = paymentServiceFactory.getPaymentService(gatewayName)
                .orElseThrow(() -> new BadRequestException(List.of(BAD_PG_MSG.formatted(gatewayName))));
        final CompletePaymentResponse response = paymentService.completePayment(completePaymentRequest);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("verify-payment")
    public ResponseEntity<VerifyPaymentResponse> verifyPayment(
            @Valid @RequestBody final VerifyPaymentRequest verifyPaymentRequest,
            final BindingResult validationResult
    ) {
        checkAndThrowValidationException(validationResult);
        final var gatewayName = verifyPaymentRequest.paymentGateway();
        final var paymentService = paymentServiceFactory.getPaymentService(gatewayName)
                .orElseThrow(() -> new BadRequestException(List.of(BAD_PG_MSG.formatted(gatewayName))));
        final VerifyPaymentResponse response = paymentService.verifyPayment(verifyPaymentRequest);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

}
