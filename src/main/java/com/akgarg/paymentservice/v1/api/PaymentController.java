package com.akgarg.paymentservice.v1.api;

import com.akgarg.paymentservice.v1.api.response.PaymentDetailResponse;
import com.akgarg.paymentservice.v1.api.response.PaymentHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String USER_ID_HEADER = "X-User-ID";

    private final PaymentService paymentService;

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailResponse> findPaymentById(
            @RequestHeader(REQUEST_ID_HEADER) final String requestId,
            @RequestHeader(USER_ID_HEADER) final String userId,
            @PathVariable("paymentId") final String paymentId) {
        final var response = paymentService.getPaymentDetailById(requestId, userId, paymentId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/history")
    public ResponseEntity<PaymentHistoryResponse> getAllPaymentDetails(
            @RequestHeader(REQUEST_ID_HEADER) final String requestId,
            @RequestHeader(USER_ID_HEADER) final String userIdFromHeader,
            @RequestParam("userId") final String userId
    ) {
        if (!userIdFromHeader.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(PaymentHistoryResponse.builder()
                            .payments(null)
                            .message("User is not authorized to view payment history")
                            .statusCode(HttpStatus.FORBIDDEN.value())
                            .build());
        }

        final var response = paymentService.getPaymentHistory(requestId, userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
