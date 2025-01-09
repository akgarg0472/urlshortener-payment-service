package com.akgarg.paymentservice.db;

import com.akgarg.paymentservice.payment.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, String> {

    List<PaymentDetail> findAllByUserIdAndDeleted(String userId, boolean deleted);

}
