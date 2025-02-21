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

    public PaymentDetailResponse getPaymentDetailById(final String userId, final String paymentId) {
        log.info("Getting payment for payment id {}", paymentId);
        final var paymentDetailOptional = databaseService.getPaymentDetails(paymentId);

        if (paymentDetailOptional.isEmpty() || !paymentDetailOptional.get().getUserId().equals(userId)) {
            log.info("No payment found for payment id {}", paymentId);
            return PaymentDetailResponse.builder()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .message("Payment not found for id: " + paymentId)
                    .build();
        }

        return PaymentDetailResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Payment found")
                .paymentDetail(PaymentDetailDto.fromPaymentDetail(paymentDetailOptional.get()))
                .build();
    }

    public PaymentHistoryResponse getPaymentHistory(final String userId) {
        log.info("Getting payment history for userId {}", userId);

        final var paymentDetails = databaseService.getAllPaymentDetails(userId)
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
