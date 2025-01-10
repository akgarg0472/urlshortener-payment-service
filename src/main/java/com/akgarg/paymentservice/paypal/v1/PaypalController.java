package com.akgarg.paymentservice.paypal.v1;

import com.akgarg.paymentservice.paypal.v1.request.CaptureOrderRequest;
import com.akgarg.paymentservice.paypal.v1.request.CreateOrderRequest;
import com.akgarg.paymentservice.paypal.v1.response.CaptureOrderResponse;
import com.akgarg.paymentservice.paypal.v1.response.CreateOrderResponse;
import com.akgarg.paymentservice.paypal.v1.response.GetOrderResponse;
import com.akgarg.paymentservice.utils.ValidationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments/paypal")
public class PaypalController {

    private final PaypalService paypalService;

    @PostMapping("/order")
    public CreateOrderResponse createOrder(@RequestHeader("X-REQUEST-ID") final String requestId,
                                           @RequestHeader("X-USER-ID") final String userId,
                                           @RequestBody @Valid final CreateOrderRequest request,
                                           final BindingResult validationResult) throws Exception {
        ValidationUtils.checkAndThrowValidationException(requestId, validationResult);
        return paypalService.createOrder(requestId, userId, request);
    }

    @PostMapping("/capture")
    public CaptureOrderResponse captureOrder(@RequestHeader("X-REQUEST-ID") final String requestId,
                                             @RequestBody @Valid final CaptureOrderRequest captureOrderRequest,
                                             final BindingResult validationResult) throws Exception {
        ValidationUtils.checkAndThrowValidationException(requestId, validationResult);
        return paypalService.captureOrder(requestId, captureOrderRequest);
    }

    @GetMapping("/order")
    public GetOrderResponse getOrder(@RequestHeader("X-REQUEST-ID") final String requestId,
                                     @RequestParam(value = "id") final String orderId) {
        return paypalService.getOrderByOrderId(requestId, orderId);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> paypalWebhook(@RequestBody final Map<String, Object> body) throws Exception {
        paypalService.processWebhook(body);
        return ResponseEntity.ok().build();
    }

}
