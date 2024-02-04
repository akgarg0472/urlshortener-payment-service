package com.akgarg.paymentservice.db;

import com.akgarg.paymentservice.exception.DatabaseException;
import com.akgarg.paymentservice.payment.PaymentDetail;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
@Service
@Profile("prod")
public class MySQLDatabaseService implements DatabaseService {
    
    @Override
    public boolean savePaymentDetails(final PaymentDetail paymentDetail) throws DatabaseException {
        return false;
    }

    @Override
    public boolean updatePaymentDetails(final PaymentDetail paymentDetail) throws DatabaseException {
        return false;
    }

    @Override
    public Optional<PaymentDetail> getPaymentDetails(final String paymentId) throws DatabaseException {
        return Optional.empty();
    }

    @Override
    public List<PaymentDetail> getAllPaymentDetails(final String userId) throws DatabaseException {
        return Collections.emptyList();
    }

    @Override
    public boolean deletePaymentDetails(final String paymentId) throws DatabaseException {
        return false;
    }

}
