package com.akgarg.paymentservice.db;

import com.akgarg.paymentservice.exception.DatabaseException;
import com.akgarg.paymentservice.payment.PaymentDetail;

import java.util.List;
import java.util.Optional;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
public interface DatabaseService {

    PaymentDetail savePaymentDetails(String requestId, PaymentDetail paymentDetail) throws DatabaseException;

    PaymentDetail updatePaymentDetails(String requestId, PaymentDetail paymentDetail) throws DatabaseException;

    Optional<PaymentDetail> getPaymentDetails(String requestId, String paymentId) throws DatabaseException;

    List<PaymentDetail> getAllPaymentDetails(String requestId, String userId) throws DatabaseException;

    boolean deletePaymentDetails(String requestId, String paymentId) throws DatabaseException;

}
