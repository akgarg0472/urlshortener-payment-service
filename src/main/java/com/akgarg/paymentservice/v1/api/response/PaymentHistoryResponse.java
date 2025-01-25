package com.akgarg.paymentservice.v1.api.response;

import com.akgarg.paymentservice.payment.PaymentDetailDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Builder
@Setter
@Getter
public class PaymentHistoryResponse {

    @JsonProperty("message")
    private String message;

    @JsonIgnore
    private int statusCode;

    @JsonProperty("payments")
    private Collection<PaymentDetailDto> payments;

}
