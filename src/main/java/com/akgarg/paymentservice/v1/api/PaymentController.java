package com.akgarg.paymentservice.v1.api;

import com.akgarg.paymentservice.v1.api.response.PaymentDetailResponse;
import com.akgarg.paymentservice.v1.api.response.PaymentHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.akgarg.paymentservice.utils.PaymentServiceUtils.USER_ID_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailResponse> findPaymentById(
            @RequestHeader(USER_ID_HEADER) final String userId,
            @PathVariable("paymentId") final String paymentId) {
        final var response = paymentService.getPaymentDetailById(userId, paymentId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/history")
    public ResponseEntity<PaymentHistoryResponse> getAllPaymentDetails(
            @RequestHeader(USER_ID_HEADER) final String userIdFromHeader,
            @RequestParam("userId") final String userId
    ) {
        if (!userIdFromHeader.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(PaymentHistoryResponse.builder()
                            .payments(null)
                            .message("You're not authorized to view payment history for some other user")
                            .statusCode(HttpStatus.FORBIDDEN.value())
                            .build());
        }

        final var response = paymentService.getPaymentHistory(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
