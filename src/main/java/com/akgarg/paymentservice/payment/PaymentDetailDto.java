package com.akgarg.paymentservice.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDetailDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("pack_id")
    private String packId;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("payment_status")
    private PaymentStatus paymentStatus;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("created_at")
    private long createdAt;

    @JsonProperty("updated_at")
    private long updatedAt;

    @JsonProperty("completed_at")
    private long completedAt;

    public static PaymentDetailDto fromPaymentDetail(final PaymentDetail paymentDetail) {
        final var paymentDetailDto = new PaymentDetailDto();
        paymentDetailDto.setId(paymentDetail.getId());
        paymentDetailDto.setPackId(paymentDetail.getPackId());
        paymentDetailDto.setAmount(paymentDetail.getAmount());
        paymentDetailDto.setCurrency(paymentDetail.getCurrency());
        paymentDetailDto.setCreatedAt(paymentDetail.getCreatedAt());
        paymentDetailDto.setUpdatedAt(paymentDetail.getUpdatedAt());
        paymentDetailDto.setPaymentMethod(paymentDetail.getPaymentMethod());
        paymentDetailDto.setPaymentStatus(PaymentStatus.valueOf(paymentDetail.getPaymentStatus()));
        paymentDetailDto.setCompletedAt(paymentDetail.getCompletedAt() != null ? paymentDetail.getCompletedAt() : 0);
        return paymentDetailDto;
    }

}
