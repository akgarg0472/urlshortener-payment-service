package com.akgarg.paymentservice.db;

import com.akgarg.paymentservice.exception.DatabaseException;
import com.akgarg.paymentservice.payment.PaymentDetail;
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
@Profile("dev")
public class InMemoryDatabaseService implements DatabaseService {

    private final Map<String, PaymentDetail> paymentDetails;

    public InMemoryDatabaseService() {
        paymentDetails = new HashMap<>();
    }

    @Override
    public boolean savePaymentDetails(final PaymentDetail paymentDetail) throws DatabaseException {
        if (paymentDetails.containsKey(paymentDetail.getPaymentId())) {
            throw new DatabaseException(
                    "Payment details already exists"
            );
        }

        paymentDetails.put(paymentDetail.getPaymentId(), paymentDetail);
        return true;
    }

    @Override
    public boolean updatePaymentDetails(final PaymentDetail paymentDetail) throws DatabaseException {
        if (!paymentDetails.containsKey(paymentDetail.getPaymentId())) {
            throw new DatabaseException(
                    "Payment details not found with id: " + paymentDetail.getPaymentId()
            );
        }

        paymentDetails.put(paymentDetail.getPaymentId(), paymentDetail);
        return true;
    }

    @Override
    public Optional<PaymentDetail> getPaymentDetails(final String paymentId) throws DatabaseException {
        return Optional.ofNullable(paymentDetails.get(paymentId));
    }

    @Override
    public List<PaymentDetail> getAllPaymentDetails(final String userId) throws DatabaseException {
        return paymentDetails
                .values()
                .stream()
                .filter(paymentDetail -> paymentDetail.getUserId().equals(userId))
                .toList();
    }

    @Override
    public boolean deletePaymentDetails(final String paymentId) throws DatabaseException {
        if (!paymentDetails.containsKey(paymentId)) {
            throw new DatabaseException(
                    "Payment details not found with id: " + paymentId
            );
        }

        return paymentDetails.remove(paymentId) != null;
    }

}
