package com.akgarg.paymentservice.payment.service;

import com.akgarg.paymentservice.db.DatabaseService;
import com.akgarg.paymentservice.eventpublisher.PaymentEvent;
import com.akgarg.paymentservice.eventpublisher.PaymentEventPublisher;
import com.akgarg.paymentservice.exception.DatabaseException;
import com.akgarg.paymentservice.exception.PaymentException;
import com.akgarg.paymentservice.payment.PaymentDetail;
import com.akgarg.paymentservice.payment.PaymentStatus;
import com.akgarg.paymentservice.request.CreatePaymentRequest;
import jakarta.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Objects;

public abstract class AbstractPaymentService implements PaymentService {

    private static final Logger LOG = LogManager.getLogger(AbstractPaymentService.class);
    private final DatabaseService databaseService;
    private final PaymentEventPublisher paymentEventPublisher;

    protected AbstractPaymentService(
            @Nonnull final DatabaseService databaseService,
            @Nonnull final PaymentEventPublisher paymentEventPublisher
    ) {
        this.databaseService = Objects.requireNonNull(databaseService, "Database service is null");
        this.paymentEventPublisher = Objects.requireNonNull(paymentEventPublisher, "Payment event publisher is null");
    }

    protected void savePaymentInDatabase(
            @Nonnull final CreatePaymentRequest createPaymentRequest,
            @Nonnull final String traceId,
            @Nonnull final String paymentId,
            @Nonnull final String paymentGatewayName
    ) throws DatabaseException {
        final PaymentDetail paymentDetail = PaymentDetailsMapper.from(createPaymentRequest);
        paymentDetail.setTraceId(traceId);
        paymentDetail.setPaymentId(paymentId);
        paymentDetail.setPaymentGateway(paymentGatewayName);

        LOG.debug("{} Payment details: {}", traceId, paymentDetail);

        final boolean isPaymentSaved = this.databaseService.savePaymentDetails(paymentDetail);

        LOG.info("{} result of payment create with order id={} -> {}", traceId, paymentId, isPaymentSaved);
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

    protected void publishPaymentEvent(@Nonnull final PaymentDetail paymentDetail) {
        Objects.requireNonNull(paymentDetail, "Payment detail is null");
        final PaymentEvent paymentEvent = new PaymentEvent(
                Objects.requireNonNull(paymentDetail.getUserId(), "userId is null"),
                Objects.requireNonNull(paymentDetail.getPlanId(), "planId is null"),
                Objects.requireNonNull(paymentDetail.getAmount(), "amount is null"),
                Objects.requireNonNull(paymentDetail.getCurrency(), "currency is null"),
                Objects.requireNonNull(paymentDetail.getPaymentGateway(), "payment gateway is null")
        );
        paymentEventPublisher.publish(paymentEvent);
    }

}
