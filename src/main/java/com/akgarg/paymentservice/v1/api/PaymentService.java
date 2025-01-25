package com.akgarg.paymentservice.v1.api;

import com.akgarg.paymentservice.payment.PaymentDetailDto;
import com.akgarg.paymentservice.v1.api.response.PaymentHistoryResponse;
import com.akgarg.paymentservice.v1.db.DatabaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final DatabaseService databaseService;

    public PaymentHistoryResponse getPaymentHistory(final String requestId, final String userId) {
        log.info("[{}] getting payment history for user {}", requestId, userId);

        final var paymentDetails = databaseService.getAllPaymentDetails(requestId, userId)
                .stream()
                .map(PaymentDetailDto::fromPaymentDetail)
                .sorted(Comparator.comparing(PaymentDetailDto::getCreatedAt))
                .toList();

        return PaymentHistoryResponse.builder()
                .message("Payment history fetched successfully")
                .statusCode(HttpStatus.OK.value())
                .payments(paymentDetails)
                .build();
    }

}
