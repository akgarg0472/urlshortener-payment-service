package com.akgarg.paymentservice.v1.api.response;

import com.akgarg.paymentservice.payment.PaymentDetailDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentDetailResponse {

    @JsonProperty("message")
    private String message;

    @JsonIgnore
    private int statusCode;

    @JsonProperty("payment_detail")
    private PaymentDetailDto paymentDetail;

}
