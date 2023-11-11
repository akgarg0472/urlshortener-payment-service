package com.akgarg.paymentservice.database;

import com.akgarg.paymentservice.payment.PaymentDetail;

import java.util.List;
import java.util.Optional;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
public interface DatabaseService {

    boolean savePaymentDetails(final PaymentDetail paymentDetail) throws DatabaseException;

    boolean updatePaymentDetails(final PaymentDetail paymentDetail) throws DatabaseException;

    Optional<PaymentDetail> getPaymentDetails(final String paymentId) throws DatabaseException;

    List<PaymentDetail> getAllPaymentDetails(final String userId) throws DatabaseException;

    boolean deletePaymentDetails(final String paymentId) throws DatabaseException;

}
