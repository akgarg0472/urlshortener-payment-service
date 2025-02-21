package com.akgarg.paymentservice.v1.paypal;

import com.akgarg.paymentservice.exception.PaymentException;
import com.akgarg.paymentservice.utils.PaymentServiceUtils;
import com.akgarg.paymentservice.v1.paypal.request.CancelPaymentRequest;
import com.akgarg.paymentservice.v1.paypal.request.CaptureOrderRequest;
import com.akgarg.paymentservice.v1.paypal.request.CreateOrderRequest;
import com.akgarg.paymentservice.v1.paypal.response.CancelPaymentResponse;
import com.akgarg.paymentservice.v1.paypal.response.CaptureOrderResponse;
import com.akgarg.paymentservice.v1.paypal.response.CreateOrderResponse;
import com.akgarg.paymentservice.v1.paypal.response.GetOrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.akgarg.paymentservice.utils.PaymentServiceUtils.REQUEST_ID_HEADER;
import static com.akgarg.paymentservice.utils.PaymentServiceUtils.USER_ID_HEADER;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments/paypal")
public class PaypalController {

    private static final String REQUEST_VALIDATION_FAILED_MSG = "Request validation failed";

    private final PaypalService paypalService;

    @PostMapping("/order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestHeader(REQUEST_ID_HEADER) final String requestId,
            @RequestHeader(USER_ID_HEADER) final String userId,
            @RequestBody @Valid final CreateOrderRequest request,
            final BindingResult validationResult) throws Exception {
        PaymentServiceUtils.checkAndThrowValidationException(validationResult);
        validateUserIds(userId, request.userId());
        final var createOrderResponse = paypalService.createOrder(request);
        createOrderResponse.setTraceId(requestId);
        return ResponseEntity.status(createOrderResponse.getStatusCode()).body(createOrderResponse);
    }

    @PostMapping("/capture")
    public ResponseEntity<CaptureOrderResponse> captureOrder(@RequestBody @Valid final CaptureOrderRequest captureOrderRequest,
                                                             final BindingResult validationResult) throws Exception {
        PaymentServiceUtils.checkAndThrowValidationException(validationResult);
        final var captureOrderResponse = paypalService.captureOrder(captureOrderRequest);
        return ResponseEntity.status(captureOrderResponse.statusCode()).body(captureOrderResponse);
    }

    @GetMapping("/order")
    public ResponseEntity<GetOrderResponse> getOrder(@RequestParam(value = "id") final String orderId) {
        final var order = paypalService.getOrderByOrderId(orderId);
        return ResponseEntity.status(order.getStatusCode()).body(order);
    }

    @PostMapping(value = "/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CancelPaymentResponse> deletePayment(
            @RequestHeader(USER_ID_HEADER) final String userId,
            @RequestBody final CancelPaymentRequest request,
            final BindingResult validationResult) {
        PaymentServiceUtils.checkAndThrowValidationException(validationResult);
        validateUserIds(userId, request.userId());
        final var response = paypalService.cancelPayment(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> paypalWebhook(@RequestBody final Map<String, Object> body) throws Exception {
        paypalService.processWebhook(body);
        return ResponseEntity.ok().build();
    }

    private void validateUserIds(final String userId, final String requestUserId) {
        if (userId != null && requestUserId != null && !userId.equals(requestUserId)) {
            throw new PaymentException(
                    HttpStatus.BAD_REQUEST.value(),
                    List.of("UserId in header and request body mismatch"),
                    REQUEST_VALIDATION_FAILED_MSG);
        }
    }

}
