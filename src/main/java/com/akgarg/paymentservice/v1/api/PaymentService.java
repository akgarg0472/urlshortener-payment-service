package com.akgarg.paymentservice.v1.api;

import com.akgarg.paymentservice.payment.PaymentDetailDto;
import com.akgarg.paymentservice.v1.api.response.PaymentDetailResponse;
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

    public PaymentDetailResponse getPaymentDetailById(final String requestId, final String userId, final String paymentId) {
        log.info("[{}] Getting payment by id: {}", requestId, paymentId);
        final var paymentDetailOptional = databaseService.getPaymentDetails(requestId, paymentId);

        if (paymentDetailOptional.isEmpty() || !paymentDetailOptional.get().getUserId().equals(userId)) {
            log.info("[{}] No payment found for id: {}", requestId, paymentId);
            return PaymentDetailResponse.builder()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .message("Payment not found with id " + paymentId)
                    .build();
        }

        return PaymentDetailResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Payment found with id " + paymentId)
                .paymentDetail(PaymentDetailDto.fromPaymentDetail(paymentDetailOptional.get()))
                .build();
    }

    public PaymentHistoryResponse getPaymentHistory(final String requestId, final String userId) {
        log.info("[{}] getting payment history for user {}", requestId, userId);

        final var paymentDetails = databaseService.getAllPaymentDetails(requestId, userId)
                .stream()
                .filter(paymentDetail -> !paymentDetail.isDeleted())
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
