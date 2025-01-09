package com.akgarg.paymentservice.db;

import com.akgarg.paymentservice.exception.DatabaseException;
import com.akgarg.paymentservice.payment.PaymentDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
@Service
@Profile("prod")
@Slf4j
@RequiredArgsConstructor
public class MySQLDatabaseService implements DatabaseService {

    private final PaymentDetailRepository paymentDetailRepository;

    @Override
    public PaymentDetail savePaymentDetails(final String requestId, final PaymentDetail paymentDetail) throws DatabaseException {
        log.info("[{}] Saving payment detail: {}", requestId, paymentDetail);
        return paymentDetailRepository.save(paymentDetail);
    }

    @Override
    public PaymentDetail updatePaymentDetails(final String requestId, final PaymentDetail paymentDetail) throws DatabaseException {
        log.info("[{}] Updating payment detail: {}", requestId, paymentDetail);
        paymentDetail.setUpdatedAt(System.currentTimeMillis());
        return paymentDetailRepository.save(paymentDetail);
    }

    @Override
    public Optional<PaymentDetail> getPaymentDetails(final String requestId, final String paymentId) throws DatabaseException {
        log.info("[{}] Getting payment detail for id: {}", requestId, paymentId);
        final var paymentDetail = paymentDetailRepository.findById(paymentId);

        if (paymentDetail.isPresent() && !paymentDetail.get().isDeleted()) {
            log.debug("[{}] Found payment detail: {}", requestId, paymentDetail);
            return paymentDetail;
        }

        return Optional.empty();
    }

    @Override
    public List<PaymentDetail> getAllPaymentDetails(final String requestId, final String userId) throws DatabaseException {
        log.info("[{}] Getting payment details for userId: {}", requestId, userId);
        return paymentDetailRepository.findAllByUserIdAndDeleted(userId, false);
    }

    @Override
    public boolean deletePaymentDetails(final String requestId, final String paymentId) throws DatabaseException {
        log.info("[{}] Deleting payment detail for id: {}", requestId, paymentId);
        final var paymentDetail = paymentDetailRepository.findById(paymentId);

        if (paymentDetail.isPresent()) {
            paymentDetail.get().setDeleted(true);
            paymentDetailRepository.save(paymentDetail.get());
            log.info("[{}] Payment detail deleted: {}", requestId, paymentDetail);
            return true;
        }

        log.info("[{}] Payment detail not found for id: {}", requestId, paymentId);

        return false;
    }

}
