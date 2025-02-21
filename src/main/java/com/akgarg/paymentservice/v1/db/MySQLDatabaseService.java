package com.akgarg.paymentservice.v1.db;

import com.akgarg.paymentservice.exception.DatabaseException;
import com.akgarg.paymentservice.payment.PaymentDetail;
import com.akgarg.paymentservice.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.akgarg.paymentservice.utils.PaymentServiceUtils.maskString;

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
    public PaymentDetail savePaymentDetails(final PaymentDetail paymentDetail) throws DatabaseException {
        log.info("Saving payment detail {}", paymentDetail);

        try {
            return paymentDetailRepository.save(paymentDetail);
        } catch (Exception e) {
            throw new DatabaseException("Failed to save payment detail", e);
        }
    }

    @Override
    public PaymentDetail updatePaymentDetails(final PaymentDetail paymentDetail) throws DatabaseException {
        log.info("Updating payment detail: {}", paymentDetail);

        try {
            paymentDetail.setUpdatedAt(System.currentTimeMillis());
            return paymentDetailRepository.save(paymentDetail);
        } catch (Exception e) {
            throw new DatabaseException("Failed to update payment detail", e);
        }
    }

    @Override
    public Optional<PaymentDetail> getPaymentDetails(final String paymentId) throws DatabaseException {
        log.info("Getting payment detail for payment id {}", maskString(paymentId));

        try {
            final var paymentDetail = paymentDetailRepository.findById(paymentId);

            if (log.isDebugEnabled()) {
                log.debug("Payment detail fetched for id {}: {}", maskString(paymentId), paymentDetail.isPresent() ? paymentDetail : null);
            }

            if (paymentDetail.isPresent() && !paymentDetail.get().isDeleted()) {
                return paymentDetail;
            }

            return Optional.empty();
        } catch (Exception e) {
            throw new DatabaseException("Failed to get payment detail for payment id " + maskString(paymentId), e);
        }
    }

    @Override
    public List<PaymentDetail> getPaymentDetailForUserByPaymentStatus(final String userId, final Collection<PaymentStatus> statuses) throws DatabaseException {
        log.info("Getting payment detail for user id {} for payment status {}", userId, statuses);

        try {
            return paymentDetailRepository.findAllByUserIdAndPaymentStatusIn(userId,
                    statuses.stream().map(Enum::name).toList()
            );
        } catch (Exception e) {
            throw new DatabaseException("Failed to get payment detail for user id " + userId, e);
        }
    }

    @Override
    public List<PaymentDetail> getAllPaymentDetails(final String userId) throws DatabaseException {
        log.info("Getting payment details for userId {}", userId);

        try {
            return paymentDetailRepository.findAllByUserIdAndDeleted(userId, false);
        } catch (Exception e) {
            throw new DatabaseException("Failed to get all payment details for userId " + userId, e);
        }
    }

}
