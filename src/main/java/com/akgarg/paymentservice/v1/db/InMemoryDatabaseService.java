package com.akgarg.paymentservice.v1.db;

import com.akgarg.paymentservice.exception.DatabaseException;
import com.akgarg.paymentservice.payment.PaymentDetail;
import com.akgarg.paymentservice.payment.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
@Service
@Slf4j
@Profile("dev")
public class InMemoryDatabaseService implements DatabaseService {

    private final Map<String, PaymentDetail> paymentDetails = new HashMap<>();

    @Override
    public PaymentDetail savePaymentDetails(final PaymentDetail paymentDetail) {
        log.info("Saving payment details: {}", paymentDetail);

        if (paymentDetails.containsKey(paymentDetail.getId())) {
            throw new DatabaseException("Payment details already exists for id %s".formatted(paymentDetail.getId()));
        }

        paymentDetails.put(paymentDetail.getId(), paymentDetail);

        return paymentDetail;
    }

    @Override
    public PaymentDetail updatePaymentDetails(final PaymentDetail paymentDetail) {
        log.info("Updating payment details: {}", paymentDetail);

        if (!paymentDetails.containsKey(paymentDetail.getId())) {
            log.error("Payment detail with id {} not found", paymentDetail.getId());
            throw new DatabaseException("Payment details not found for payment id " + paymentDetail.getId());
        }

        paymentDetail.setUpdatedAt(System.currentTimeMillis());
        paymentDetails.put(paymentDetail.getId(), paymentDetail);
        return paymentDetail;
    }

    @Override
    public Optional<PaymentDetail> getPaymentDetails(final String paymentId) {
        log.info("Getting payment details for id: {}", paymentId);
        return Optional.ofNullable(paymentDetails.get(paymentId));
    }

    @Override
    public List<PaymentDetail> getPaymentDetailForUserByPaymentStatus(final String userId, final Collection<PaymentStatus> statuses) {
        log.info("Getting payment details for user id {} with payment status {}", userId, statuses);
        return paymentDetails
                .values()
                .stream()
                .filter(pd -> pd.getUserId().equals(userId))
                .filter(pd -> statuses.contains(PaymentStatus.valueOf(pd.getPaymentStatus())))
                .toList();
    }

    @Override
    public List<PaymentDetail> getAllPaymentDetails(final String userId) {
        log.info("Getting payment details for userId {}", userId);
        return paymentDetails
                .values()
                .stream()
                .filter(pd -> pd.getUserId().equals(userId) && !pd.isDeleted())
                .toList();
    }

}
