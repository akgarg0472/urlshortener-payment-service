package com.akgarg.paymentservice.v1.api;

import com.akgarg.paymentservice.v1.api.response.PaymentHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String USER_ID_HEADER = "X-User-ID";

    private final PaymentService paymentService;

    @GetMapping("/history")
    public ResponseEntity<PaymentHistoryResponse> getAllPaymentDetails(
            @RequestHeader(REQUEST_ID_HEADER) final String requestId,
            @RequestHeader(USER_ID_HEADER) final String userId
    ) {
        final var response = paymentService.getPaymentHistory(requestId, userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
