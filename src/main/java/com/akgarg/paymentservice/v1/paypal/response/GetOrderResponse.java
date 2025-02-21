package com.akgarg.paymentservice.v1.paypal.response;

import com.akgarg.paymentservice.payment.PaymentDetailDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public final class GetOrderResponse {

    @JsonProperty("status_code")
    private final int statusCode;

    @JsonProperty("trace_id")
    private final String traceId;

    @JsonProperty("message")
    private final String message;

    @Nullable
    @JsonProperty("data")
    private final PaymentDetailDto paymentDetail;

}
