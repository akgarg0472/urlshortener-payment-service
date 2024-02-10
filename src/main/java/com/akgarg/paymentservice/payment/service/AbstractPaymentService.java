package com.akgarg.paymentservice.payment.service;

import com.akgarg.paymentservice.db.DatabaseService;
import com.akgarg.paymentservice.exception.DatabaseException;
import com.akgarg.paymentservice.exception.PaymentException;
import com.akgarg.paymentservice.payment.PaymentDetail;
import com.akgarg.paymentservice.payment.PaymentStatus;
import com.akgarg.paymentservice.request.CreatePaymentRequest;
import jakarta.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public abstract class AbstractPaymentService implements PaymentService {

    private static final Logger LOG = LogManager.getLogger(AbstractPaymentService.class);
    private final DatabaseService databaseService;

    protected AbstractPaymentService(@Nonnull final DatabaseService databaseService) {
        this.databaseService = Objects.requireNonNull(databaseService, "Database service is null");
    }

    protected void savePaymentInDatabase(
            @Nonnull final CreatePaymentRequest createPaymentRequest,
            @Nonnull final String traceId,
            @Nonnull final String orderId,
            @Nonnull final String paymentGatewayName
    ) throws DatabaseException {
        final PaymentDetail paymentDetail = new PaymentDetail(
                traceId,
                orderId,
                createPaymentRequest.userId(),
                createPaymentRequest.amount(),
                PaymentStatus.CREATED,
                createPaymentRequest.currency(),
                createPaymentRequest.paymentMethod(),
                Instant.now(),
                null,
                paymentGatewayName
        );

        LOG.debug("{} Payment details: {}", traceId, paymentDetail);

        final boolean isPaymentSaved = this.databaseService.savePaymentDetails(paymentDetail);

        LOG.info("{} result of payment create with order id={} -> {}", traceId, orderId, isPaymentSaved);
    }

    protected PaymentDetail getPaymentDetailByPaymentId(@Nonnull final String paymentId) throws DatabaseException, PaymentException {
        return this.databaseService
                .getPaymentDetails(paymentId)
                .orElseThrow(() -> new PaymentException(
                        HttpStatus.NOT_FOUND.value(),
                        null,
                        "Payment details not found for payment id=" + paymentId
                ));
    }

    protected void updatePaymentDetails(@Nonnull final PaymentDetail paymentDetail) throws DatabaseException {
        final boolean isPaymentDetailUpdated = this.databaseService.updatePaymentDetails(paymentDetail);
        LOG.info("{} Payment detail update result: {}", paymentDetail.getTraceId(), isPaymentDetailUpdated);
    }

    protected void validatePaymentStatusForPaymentCompletion(final PaymentDetail paymentDetail) throws PaymentException {
        if (!PaymentStatus.CREATED.equals(paymentDetail.getPaymentStatus())) {
            LOG.error(
                    "{} complete payment validation check failed. Invalid payment status:{}, payment id={}",
                    paymentDetail.getTraceId(),
                    paymentDetail.getPaymentStatus(),
                    paymentDetail.getPaymentId()
            );

            throw new PaymentException(
                    HttpStatus.BAD_REQUEST.value(),
                    List.of("Invalid payment status: " + paymentDetail.getPaymentStatus()),
                    "Failed to complete payment"
            );
        }
    }

}
