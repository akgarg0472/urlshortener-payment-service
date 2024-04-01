package com.akgarg.paymentservice.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
@Getter
@Setter
@Entity
public final class PaymentDetail {

    @Id
    private String paymentId;

    private String traceId;
    private String userId;
    private String planId;
    private Long amount;
    private PaymentStatus paymentStatus;
    private String currency;
    private String paymentMethod;
    private Instant paymentCreatedAt;
    private Instant paymentCompletedAt;
    private String paymentGateway;

}
