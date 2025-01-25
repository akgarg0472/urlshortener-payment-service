package com.akgarg.paymentservice.payment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "payment_detail", indexes = {
        @Index(name = "idx_payment_detail_user_id", columnList = "user_id")
})
public final class PaymentDetail {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "pack_id", nullable = false)
    private String packId;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "status", nullable = false)
    private String paymentStatus;

    @Column(name = "gateway", nullable = false)
    private String paymentGateway;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "created_at", nullable = false)
    private long createdAt;

    @Column(name = "updated_at", nullable = false)
    private long updatedAt;

    @Column(name = "completed_at")
    private Long completedAt;

    @Column(name = "is_deleted")
    private boolean deleted = false;

}
