package com.akgarg.paymentservice.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDetailDto {

    private String id;
    private String packId;
    private String amount;
    private PaymentStatus paymentStatus;
    private String currency;
    private String paymentMethod;
    private long createdAt;
    private long updatedAt;

    public static PaymentDetailDto fromPaymentDetail(final PaymentDetail paymentDetail) {
        final var paymentDetailDto = new PaymentDetailDto();
        paymentDetailDto.setId(paymentDetail.getId());
        paymentDetailDto.setPackId(paymentDetail.getPackId());
        paymentDetailDto.setAmount(paymentDetail.getAmount());
        paymentDetailDto.setCurrency(paymentDetail.getCurrency());
        paymentDetailDto.setPaymentMethod(paymentDetail.getPaymentMethod());
        paymentDetailDto.setCreatedAt(paymentDetailDto.getCreatedAt());
        paymentDetailDto.setUpdatedAt(paymentDetailDto.getUpdatedAt());
        return paymentDetailDto;
    }

}
