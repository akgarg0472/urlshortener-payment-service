package com.akgarg.paymentservice.paypal;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.akgarg.paymentservice.utils.ValidationUtils.checkAndThrowValidationException;

@RestController
@RequestMapping("/api/v1/payments/paypal")
class PaypalController {

    private final PaypalService paypalService;

    public PaypalController(final PaypalService paypalService) {
        this.paypalService = paypalService;
    }

    @PostMapping("/create")
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @Valid @RequestBody final CreatePaymentRequest createPaymentRequest,
            final BindingResult validationResult
    ) {
        checkAndThrowValidationException(validationResult);
        final CreatePaymentResponse paymentResponse = paypalService.createPayment(createPaymentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentResponse);
    }

    @PostMapping("complete")
    public ResponseEntity<CompletePaymentResponse> completePayment(
            @Valid @RequestBody final CompletePaymentRequest completePaymentRequest,
            final BindingResult validationResult
    ) {
        checkAndThrowValidationException(validationResult);
        final CompletePaymentResponse response = paypalService.completePayment(completePaymentRequest);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

}
