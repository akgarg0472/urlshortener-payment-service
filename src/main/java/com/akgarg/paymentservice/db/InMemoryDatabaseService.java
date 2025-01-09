package com.akgarg.paymentservice.db;

import com.akgarg.paymentservice.exception.DatabaseException;
import com.akgarg.paymentservice.payment.PaymentDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
@Service
@Slf4j
@Profile("dev")
public class InMemoryDatabaseService implements DatabaseService {

    private final Map<String, PaymentDetail> paymentDetails;

    public InMemoryDatabaseService() {
        paymentDetails = new HashMap<>();
    }

    @Override
    public PaymentDetail savePaymentDetails(final String requestId, final PaymentDetail paymentDetail) throws DatabaseException {
        log.info("[{}] Saving payment details: {}", requestId, paymentDetail);

        if (paymentDetails.containsKey(paymentDetail.getId())) {
            throw new DatabaseException(
                    "Payment details already exists"
            );
        }

        paymentDetails.put(paymentDetail.getId(), paymentDetail);

        return paymentDetail;
    }

    @Override
    public PaymentDetail updatePaymentDetails(final String requestId, final PaymentDetail paymentDetail) throws DatabaseException {
        log.info("[{}] Updating payment details: {}", requestId, paymentDetail);

        if (!paymentDetails.containsKey(paymentDetail.getId())) {
            log.error("[{}] Payment detail with id {} not found", requestId, paymentDetail.getId());

            throw new DatabaseException(
                    "Payment details not found with id: " + paymentDetail.getId()
            );
        }

        paymentDetail.setUpdatedAt(System.currentTimeMillis());
        paymentDetails.put(paymentDetail.getId(), paymentDetail);
        return paymentDetail;
    }

    @Override
    public Optional<PaymentDetail> getPaymentDetails(final String requestId, final String paymentId) throws DatabaseException {
        log.info("[{}] Getting payment details for id: {}", requestId, paymentId);
        return Optional.ofNullable(paymentDetails.get(paymentId));
    }

    @Override
    public List<PaymentDetail> getAllPaymentDetails(final String requestId, final String userId) throws DatabaseException {
        log.info("[{}] Getting payment details: {}", requestId, userId);

        return paymentDetails
                .values()
                .stream()
                .filter(pd -> pd.getUserId().equals(userId) && !pd.isDeleted())
                .toList();
    }

    @Override
    public boolean deletePaymentDetails(final String requestId, final String paymentId) throws DatabaseException {
        log.info("[{}] Deleting payment details for id: {}", requestId, paymentId);

        if (!paymentDetails.containsKey(paymentId)) {
            log.error("[{}] Payment detail not found with id: {}", requestId, paymentId);

            throw new DatabaseException(
                    "Payment details not found with id: " + paymentId
            );
        }

        paymentDetails.get(paymentId).setDeleted(true);
        return true;
    }

}
