package com.akgarg.paymentservice.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
@Getter
@Setter
@AllArgsConstructor
public final class PaymentDetail {

    private String traceId;
    private String paymentId;
    private String userId;
    private Long amount;
    private PaymentStatus paymentStatus;
    private String currency;
    private String paymentMethod;
    private Instant paymentCreatedAt;
    private Instant paymentCompletedAt;
    private String paymentGateway;

}
