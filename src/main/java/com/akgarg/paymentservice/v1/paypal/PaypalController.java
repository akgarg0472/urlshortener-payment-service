package com.akgarg.paymentservice.v1.paypal;

import com.akgarg.paymentservice.exception.PaymentException;
import com.akgarg.paymentservice.utils.ValidationUtils;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments/paypal")
public class PaypalController {

    private static final String REQUEST_VALIDATION_FAILED_MSG = "Request validation failed";
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String USER_ID_HEADER = "X-User-ID";

    private final PaypalService paypalService;

    @PostMapping("/order")
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestHeader(REQUEST_ID_HEADER) final String requestId,
                                                           @RequestHeader(USER_ID_HEADER) final String userId,
                                                           @RequestBody @Valid final CreateOrderRequest request,
                                                           final BindingResult validationResult) throws Exception {
        ValidationUtils.checkAndThrowValidationException(requestId, validationResult);
        validateUserIds(requestId, userId, request.userId());
        final var createOrderResponse = paypalService.createOrder(requestId, request);
        return ResponseEntity.status(createOrderResponse.getStatusCode()).body(createOrderResponse);
    }

    @PostMapping("/capture")
    public ResponseEntity<CaptureOrderResponse> captureOrder(@RequestHeader(REQUEST_ID_HEADER) final String requestId,
                                                             @RequestBody @Valid final CaptureOrderRequest captureOrderRequest,
                                                             final BindingResult validationResult) throws Exception {
        ValidationUtils.checkAndThrowValidationException(requestId, validationResult);
        final var captureOrderResponse = paypalService.captureOrder(requestId, captureOrderRequest);
        return ResponseEntity.status(captureOrderResponse.statusCode()).body(captureOrderResponse);
    }

    @GetMapping("/order")
    public ResponseEntity<GetOrderResponse> getOrder(@RequestHeader(REQUEST_ID_HEADER) final String requestId,
                                                     @RequestParam(value = "id") final String orderId) {
        final var order = paypalService.getOrderByOrderId(requestId, orderId);
        return ResponseEntity.status(order.statusCode()).body(order);
    }

    @PostMapping(value = "/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CancelPaymentResponse> deletePayment(
            @RequestHeader(REQUEST_ID_HEADER) final String requestId,
            @RequestHeader(USER_ID_HEADER) final String userId,
            @RequestBody final CancelPaymentRequest request,
            final BindingResult validationResult) {
        ValidationUtils.checkAndThrowValidationException(requestId, validationResult);
        validateUserIds(requestId, userId, request.userId());
        final var response = paypalService.cancelPayment(requestId, request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> paypalWebhook(@RequestBody final Map<String, Object> body) throws Exception {
        paypalService.processWebhook(body);
        return ResponseEntity.ok().build();
    }

    private void validateUserIds(final String requestId, final String userId, final String requestUserId) {
        if (userId != null && requestUserId != null && !userId.equals(requestUserId)) {
            log.warn("[{}] User id is not the same as the requested user id", requestId);
            throw new PaymentException(requestId,
                    HttpStatus.BAD_REQUEST.value(),
                    List.of("UserId in header and request body mismatch"),
                    REQUEST_VALIDATION_FAILED_MSG);
        }
    }

}
