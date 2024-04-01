package com.akgarg.paymentservice.db;

import com.akgarg.paymentservice.payment.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, String > {
}
